package com.rundeck.plugins.ansible.util;

import com.dtolabs.rundeck.core.execution.proxy.DefaultSecretBundle;
import com.dtolabs.rundeck.core.execution.proxy.SecretBundle;
import com.dtolabs.utils.Streams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import com.rundeck.plugins.ansible.ansible.AnsibleRunnerBuilder;

public class AnsibleUtil {

    public static SecretBundle createBundle(AnsibleRunnerBuilder ansibleRunnerBuilder)  {
        try {
            DefaultSecretBundle secretBundle = new DefaultSecretBundle();

            if(ansibleRunnerBuilder.getPasswordStoragePath() != null) {
                secretBundle.addSecret(
                        ansibleRunnerBuilder.getPasswordStoragePath(),
                        ansibleRunnerBuilder.getPasswordStorageData()
                );
            }
            if(ansibleRunnerBuilder.getPrivateKeyfilePath() != null) {
                ByteArrayOutputStream pkData = new ByteArrayOutputStream();
                Streams.copyStream(ansibleRunnerBuilder.getPrivateKeyStorageData(), pkData);
                secretBundle.addSecret(
                        ansibleRunnerBuilder.getPrivateKeyStoragePath(),
                        pkData.toByteArray()
                );
            }

            return secretBundle;
        } catch(IOException iex) {
            throw new RuntimeException("Unable to prepare secret bundle", iex);
        }
    }

}
