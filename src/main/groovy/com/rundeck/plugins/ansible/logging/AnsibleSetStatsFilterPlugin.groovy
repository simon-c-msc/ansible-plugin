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

package com.rundeck.plugins.ansible.logging

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

import java.util.Iterator
import com.google.gson.JsonParser
import com.google.gson.JsonObject
import com.google.gson.JsonElement


/**
 * @author Simon Cateau
 * @since 20/09/2023
 */
@Plugin(name = "ansible-set-stats", service = "LogFilter")
@PluginDescription(title = "Ansible set_stats",
                   description = '''Captures simple Key/Value data from the output of the ansible set_stats module.\n\n
In order for custom stats to be displayed, you must set show_custom_stats in section [defaults] in ansible.cfg or by defining environment variable ANSIBLE_SHOW_CUSTOM_STATS to true.  
''')

class AnsibleSetStatsFilterPlugin implements LogFilterPlugin{
    @PluginProperty(
        title = "Log Data",
        description = "If true, log the captured data",
        defaultValue = 'false'
    )
    Boolean outputData

    Pattern setStatsGlobalPattern
    OutputContext outputContext
    Map<String, String> allData
    ObjectMapper mapper

    @Override
    void init(final PluginLoggingContext context) {
        String regex = /^\tRUN:\s(\{.*\})$/
        setStatsGlobalPattern = Pattern.compile(regex)
        outputContext = context.getOutputContext()
        mapper = new ObjectMapper()
        allData = [:]
    }

    @Override
    void handleEvent(final PluginLoggingContext context, final LogEventControl event) {
        if (event.eventType == 'log' && event.loglevel == LogLevel.NORMAL && event.message?.length() > 0) {
            Matcher match = setStatsGlobalPattern.matcher(event.message)

            if(match.matches()){
                String jsonString = match.group(1)
                JsonObject obj = JsonParser.parseString(jsonString).getAsJsonObject()
                Iterator<String> keys = obj.keySet().iterator()
                while(keys.hasNext()) {
                        String key = keys.next()
                        String value =  obj.get(key).getAsString()
                        allData[key] = value
                        outputContext.addOutput("data", key, value)
                }
            }
        }
    }

    @Override
    void complete(final PluginLoggingContext context) {
        if (allData) {
            if (outputData) {
                context.log(
                        2,
                        mapper.writeValueAsString(allData),
                        [
                                'content-data-type'       : 'application/json',
                                'content-meta:table-title': 'Ansible set_stats: Results'
                        ]
                )
            }
            // allData = [:]
        }
    }
}
