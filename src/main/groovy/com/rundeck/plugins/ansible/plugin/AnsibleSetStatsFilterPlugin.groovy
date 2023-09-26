/*
 * Copyright 2023 MSC Technology. (https://msc-technology.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//package com.dtolabs.rundeck.server.plugins.logging

import com.dtolabs.rundeck.core.execution.workflow.OutputContext
import com.dtolabs.rundeck.core.logging.LogEventControl
import com.dtolabs.rundeck.core.logging.LogLevel
import com.dtolabs.rundeck.core.logging.PluginLoggingContext
import com.dtolabs.rundeck.core.plugins.Plugin
import com.dtolabs.rundeck.core.plugins.configuration.PropertyValidator
import com.dtolabs.rundeck.core.plugins.configuration.ValidationException
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty
import com.dtolabs.rundeck.plugins.descriptions.RenderingOption
import com.dtolabs.rundeck.plugins.descriptions.RenderingOptions
import com.dtolabs.rundeck.plugins.logging.LogFilterPlugin
import com.fasterxml.jackson.databind.ObjectMapper

import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

import org.grails.web.json.JSONObject
import java.util.Iterator

/**
 * @author Simon Cateau
 * @since 20/09/2023
 */

rundeckPlugin(LogFilterPlugin){
    title = "Ansible set_stats"
    description = "Captures simple Key/Value data from the output of the ansible set_stats module."
    version = "0.0.1"

    configuration {
        outputData (title: "Log Data", description: "If true, log the captured data", defaultValue: 'false', type: 'Boolean')
    }

    Pattern setStatsGlobalPattern
    OutputContext outputContext
    Map<String, String> allData
    ObjectMapper mapper

    init { PluginLoggingContext context, Map configuration ->
        String regex = /^\tRUN:\s(\{.*\})$/
        setStatsGlobalPattern = Pattern.compile(regex)
        outputContext = context.getOutputContext()
        mapper = new ObjectMapper()
        allData = [:]
    }

    handleEvent { PluginLoggingContext context, LogEventControl event, Map configuration ->
        if (event.eventType == 'log' && event.loglevel == LogLevel.NORMAL && event.message?.length() > 0) {
            Matcher match = setStatsGlobalPattern.matcher(event.message)

            if(match.matches()){
                String jsonString = match.group(1)
                JSONObject obj = new JSONObject(jsonString)
                Iterator<String> keys = obj.keys();
                while(keys.hasNext()) {
                        String key = keys.next()
                        Object value = obj.get(key)
                        allData[key] = value.toString()
                        outputContext.addOutput("data", key, value.toString())
                }
            }
        }
    }

    complete { PluginLoggingContext context, Map configuration ->
        if (allData) {
            if (configuration.outputData == 'true') {
                context.log(
                        2,
                        mapper.writeValueAsString(allData),
                        [
                                'content-data-type'       : 'application/json',
                                'content-meta:table-title': 'Ansible set_stats: Results'
                        ]
                )
            }
            allData = [:]
        }
    }

}