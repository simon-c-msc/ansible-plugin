package com.rundeck.plugins.ansible.ansible;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
      Map<String, JsonElement> attributesJson = new HashMap<String, JsonElement>();

      // Add ALL node attributes as inventory host variables
      for (Entry<String, String> attribute : attributes.entrySet() ) {
        JsonElement json;
        try {
          JsonReader reader = new JsonReader(new StringReader(attribute.getValue()));
          reader.setLenient(true);
          json = new JsonParser().parse(reader);
        } catch (Exception e) {
          // cannot be parsed as Json. ie: /INT
          json = new JsonPrimitive(attribute.getValue()) ;
        }
        
        // avoid JsonParser truncate after ' ' or ','
        // attributes are not valid serialized Json
        if (json.isJsonPrimitive()) {
          json = new JsonPrimitive(attribute.getValue()) ;
        }

        attributesJson.put(attribute.getKey(), json);
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
