package com.rundeck.plugins.ansible.ansible;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

public class AnsibleInventory {

  public class AnsibleInventoryHosts {

    protected Map<String, Map<String, JsonElement>> hosts = new HashMap<String, Map<String, JsonElement>>();
    protected Map<String, AnsibleInventoryHosts> children = new HashMap<String, AnsibleInventoryHosts>();

    public AnsibleInventoryHosts addHost(String nodeName) {
      hosts.put(nodeName, new HashMap<String, JsonElement>());
      return this;
    }

    public AnsibleInventoryHosts addHost(String nodeName, String host, Map<String, String> attributes) {
      attributes.put("ansible_host", host);

      // deserialize 'String attribute.value' to JsonArray or JsonElement, when not a Primitive
      Map<String, JsonElement> attributesJson = new HashMap<String, JsonElement>();
      
      for (String attribute : attributes.keySet()) {
        System.out.println("attribute '" + attribute + "' = '"+ attributes.get(attribute) + "'");
        
        JsonElement json;
        try {
          JsonReader reader = new JsonReader(new StringReader(attributes.get(attribute)));
          reader.setLenient(true);
          json = new JsonParser().parse(reader);
        } catch (Exception e) {
          // cannot be parsed as Json. ie: /INT
          System.out.println("Attribute '" + attribute + "' has invalid Json: '"+ attributes.get(attribute) + "'");
          json = new JsonPrimitive(attributes.get(attribute)) ;
        }
        
        System.out.println("parsed json: " + json);

        // OK
        if (json.isJsonObject()) System.out.println("attribute " + attribute + " is jsonObject");
        if (json.isJsonArray()) System.out.println("attribute " + attribute + " is JsonArray");
        if (json.isJsonNull()) System.out.println("attribute " + attribute + " is JsonNull");

        // KO : ',' or ' ' truncates
        if (json.isJsonPrimitive()) {
          System.out.println("attribute " + attribute + " is JsonPrimitive");
          json = new JsonPrimitive(attributes.get(attribute)) ;
        }

        // isJsonArray => OK
        // jsonObject => OK
        // isJsonNull : '' => null => OK
        // catch Parser exception -> Primitive : OK

        // isJsonPrimitive => KO
        //   attribute 'domain_dc' = 'intb-com,DC=local'
        //   ==> "domain_dc: intb-com",
        //   attribute 'description' = 'Debian GNU/Linux 9.12 (stretch)'
        //   ==> "description: Debian",
        //   attribute 'bcomssh_ssh_ca' = 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQDKzHj7JB9f0I7RVR6UgpgT1LBEBT9n6yQ3NW7RieMvcPOlJ1nvjNcVLGAr8bx9yMrKBwbZ6FMrfsEduNjFzH/KoOHfaNV23Dy4YUsK5j9j7oEjai1bkGmWAJRxbH6qSMk4EJXWQB8A0g5VUBnkfe6HWWd2giGuayUHGIuvV5EePYq/hYd8Uc9mDDPwZJL5vZjxr+Rox9eDmF7E1wvryULli3cGINkYKK7Ho9SdfTTR+mUfr33RzrNa2YlTaleebIhR1qk0WZhr8DdVNi2QxHyI9c+rZkYUePRh7Ycs2PZxGJxUJVPkUuPzP9CvfJar7OXawNqduON+TAi8fqMt0LXK4rPq8B77Xu1URix+Qz2w3/eiI+7ubHi8YWiGD1UPm1YDmKHHf9ykhMBgpusYoVSyMuWC3tqb+SIZm+7Y1/0IaF7/R3gJahfTPo0zRVDzctCKD9dtUs85S46i3Hho13+4jr41bLFh/xjyxB7Wqt8B+NaUDmo8QAsWIB03jTU6Pm1IF5GhIqNZYH8kRuHqmquKNThteq9/s8Iis4gN0izX++i+zbqODWSQOlC9J3Qlx0PEfnDg86lKb9XIyyOY98uoWx2400BP7vIwUg+0wRIy1HB2C9RurC5bozKEim39Ut3dQ3xVdZ0xcp52DqpOy20Qz9h6K5XfJd6AwjqxvOoicQ== BCOM SSH CA ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQC7ZRf5LDtuQkg4dBzm8EJ+qyTwpUIL0ZtuWTXoz0hKssIliG/76kESnVzzAhLuhplhxkD6IB5XvHbQCU2Vq1Q8/izK/rOsdiTRWG2ccg3UGBlpVfEjvSbeRwyo9p75HuKt1BtGWBTYGq0FPQcgdjxzUuBAdQ91mUBj40iA88s8jwncKYmaDynLINRzuVtPZlBiedQy8GCv8K0zwYZHOcI7POFH0+XNLl1mU0D/asHKjOzr2vmhNLMa0Z57KgiBeI9UrNuGOSw469GFhKEYal7wnJrFdNwsh8HZPc1dYoyoB1UvtYZvXwkIPl1dAwt+sBCs3BVn28SGpyrBD2zMQ10JcE5oN2b6dL4Npkn3CRSl01wssk3Y8dfWBH358ShExnLJC1PWDL+YQ91nITooeFJMuZm/kMHvqX3ZKa9lD2r6y2VnHq7Itrwk9CQJknbuKiwp39QWnWoueH0bcTXxlSMUBLmtrRj0iW9MH+t0jYwPqk13+yK24s0hg2FeTdXTJqll/yTc+VZxh6VDBySZZTcBDKuKGlYbk4DBgAFREY4hShNUCna6TvEIz/LQSw6jL7h6eNyuopmj5jDyBEgy9kNxvDTO3q+W/IlfWuWa3sAFU6C+9p0KaWaugfLah3xYhracf68hpvl62bk7nyhO5cYnh0g0St515NlujQz8q3/GBw== BCOM SSH CA INT '
        //   ==> "bcomssh_ssh_ca: ssh-rsa",

        attributesJson.put(attribute, json);
      }

      hosts.put(nodeName, attributesJson);
      return this;
    }

    public AnsibleInventoryHosts getOrAddChildHostGroup(String groupName) {
      children.putIfAbsent(groupName, new AnsibleInventoryHosts());
      return children.get(groupName);
    }
  }

  protected AnsibleInventoryHosts all = new AnsibleInventoryHosts();

  public AnsibleInventory addHost(String nodeName, String host, Map<String, String> attributes) {
    // Remove attributes that are reserved in Ansible
    String[] reserved = { "hostvars", "group_names", "groups", "environment" };
    for (String r: reserved){
      attributes.remove(r);
    }
    all.addHost(nodeName, host, attributes);
    // Create Ansible groups by attribute
    // Group by osFamily is needed for windows hosts setup
    String[] attributeGroups = { "osFamily", "tags" };
    for (String g: attributeGroups) {
      if (attributes.containsKey(g)) {
        String[] groupNames = attributes.get(g).toLowerCase().split(",");
        for (String groupName: groupNames) {
          all.getOrAddChildHostGroup(groupName.trim()).addHost(nodeName);
        }
      }
    }
    return this;
  }
}
