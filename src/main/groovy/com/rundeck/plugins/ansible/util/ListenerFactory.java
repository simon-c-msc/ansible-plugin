package com.rundeck.plugins.ansible.util;

import java.io.*;

public abstract class ListenerFactory {

      public static Listener getListener(final PrintStream stream) {            
        return new Listener() {
                @Override
                public void output(String line) {
                    stream.println(line);
                }
              };
      }
}
