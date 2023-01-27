package com.rundeck.plugins.ansible.util;

import com.dtolabs.rundeck.core.execution.proxy.DefaultSecretBundle;
import com.dtolabs.rundeck.core.execution.proxy.SecretBundle;
import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
import com.rundeck.plugins.ansible.ansible.AnsibleRunnerBuilder;

import java.io.IOException;

public class AnsibleUtil {


    public static SecretBundle createBundle(AnsibleRunnerBuilder builder){

        DefaultSecretBundle secretBundle = new DefaultSecretBundle();

        try {
            if(builder.getPasswordStoragePath()!=null){
                secretBundle.addSecret(
                        builder.getPasswordStoragePath(),
                        builder.getPasswordStorageData()
                );
            }

            if(builder.getPrivateKeyStoragePath()!=null){
                secretBundle.addSecret(
                        builder.getPrivateKeyStoragePath(),
                        builder.getPrivateKeyStorageDataBytes()
                );
            }

            if(builder.getBecomePasswordStoragePath()!=null){
                secretBundle.addSecret(
                        builder.getBecomePasswordStoragePath(),
                        builder.getBecomePasswordStorageData()
                );
            }

            return secretBundle;

        } catch (Exception e) {
            return null;
        }


    }
}
