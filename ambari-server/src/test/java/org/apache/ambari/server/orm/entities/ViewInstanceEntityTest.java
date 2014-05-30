/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ambari.server.orm.entities;

import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.view.configuration.InstanceConfig;
import org.apache.ambari.server.view.configuration.InstanceConfigTest;
import org.apache.ambari.view.ResourceProvider;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.easymock.EasyMock.createNiceMock;

/**
 * ViewInstanceEntity tests.
 */
public class ViewInstanceEntityTest {

  private static String xml_with_instance_label = "<view>\n" +
      "    <name>MY_VIEW</name>\n" +
      "    <label>My View!</label>\n" +
      "    <version>1.0.0</version>\n" +
      "    <instance>\n" +
      "        <name>INSTANCE1</name>\n" +
      "        <label>My Instance 1!</label>\n" +
      "    </instance>\n" +
      "</view>";

  private static String xml_without_instance_label = "<view>\n" +
      "    <name>MY_VIEW</name>\n" +
      "    <label>My View!</label>\n" +
      "    <version>1.0.0</version>\n" +
      "    <instance>\n" +
      "        <name>INSTANCE1</name>\n" +
      "    </instance>\n" +
      "</view>";

  @Test
  public void testGetViewEntity() throws Exception {
    InstanceConfig instanceConfig = InstanceConfigTest.getInstanceConfigs().get(0);
    ViewEntity viewDefinition = ViewEntityTest.getViewEntity();
    ViewInstanceEntity viewInstanceDefinition = new ViewInstanceEntity(viewDefinition, instanceConfig);

    Assert.assertEquals(viewDefinition, viewInstanceDefinition.getViewEntity());
  }

  @Test
  public void testGetConfiguration() throws Exception {
    InstanceConfig instanceConfig = InstanceConfigTest.getInstanceConfigs().get(0);
    ViewEntity viewDefinition = ViewEntityTest.getViewEntity();
    ViewInstanceEntity viewInstanceDefinition = new ViewInstanceEntity(viewDefinition, instanceConfig);

    Assert.assertEquals(instanceConfig, viewInstanceDefinition.getConfiguration());
  }

  @Test
  public void testGetName() throws Exception {
    ViewInstanceEntity viewInstanceDefinition = getViewInstanceEntity();

    Assert.assertEquals("INSTANCE1", viewInstanceDefinition.getName());
  }

  @Test
  public void testGetLabel() throws Exception {
    // with an instance label
    InstanceConfig instanceConfig = InstanceConfigTest.getInstanceConfigs(xml_with_instance_label).get(0);
    ViewEntity viewDefinition = ViewEntityTest.getViewEntity();
    ViewInstanceEntity viewInstanceDefinition = new ViewInstanceEntity(viewDefinition, instanceConfig);

    Assert.assertEquals("My Instance 1!", viewInstanceDefinition.getLabel());

    // without an instance label
    instanceConfig = InstanceConfigTest.getInstanceConfigs(xml_without_instance_label).get(0);
    viewDefinition = ViewEntityTest.getViewEntity();
    viewInstanceDefinition = new ViewInstanceEntity(viewDefinition, instanceConfig);

    // should default to view label
    Assert.assertEquals("My View!", viewInstanceDefinition.getLabel());
  }

  @Test
  public void testAddGetProperty() throws Exception {
    ViewInstanceEntity viewInstanceDefinition = getViewInstanceEntity();

    viewInstanceDefinition.putProperty("p1", "v1");
    viewInstanceDefinition.putProperty("p2", "v2");
    viewInstanceDefinition.putProperty("p3", "v3");

    Map<String, String> properties = viewInstanceDefinition.getPropertyMap();

    Assert.assertEquals(3, properties.size());

    Assert.assertEquals("v1", properties.get("p1"));
    Assert.assertEquals("v2", properties.get("p2"));
    Assert.assertEquals("v3", properties.get("p3"));
  }

  @Test
  public void testAddGetService() throws Exception {
    ViewInstanceEntity viewInstanceDefinition = getViewInstanceEntity();

    Object service = new Object();

    viewInstanceDefinition.addService("resources", service);

    Object service2 = new Object();

    viewInstanceDefinition.addService("subresources", service2);

    Assert.assertEquals(service, viewInstanceDefinition.getService("resources"));
    Assert.assertEquals(service2, viewInstanceDefinition.getService("subresources"));
  }

  @Test
  public void testAddGetResourceProvider() throws Exception {
    ViewInstanceEntity viewInstanceDefinition = getViewInstanceEntity();

    ResourceProvider provider = createNiceMock(ResourceProvider.class);
    Resource.Type type = new Resource.Type("MY_VIEW{1.0.0}/myType");

    viewInstanceDefinition.addResourceProvider(type, provider);

    Assert.assertEquals(provider, viewInstanceDefinition.getResourceProvider(type));
    Assert.assertEquals(provider, viewInstanceDefinition.getResourceProvider("myType"));
  }

  @Test
  public void testContextPath() throws Exception {
    ViewInstanceEntity viewInstanceDefinition = getViewInstanceEntity();

    Assert.assertEquals(ViewInstanceEntity.VIEWS_CONTEXT_PATH_PREFIX + "MY_VIEW/1.0.0/INSTANCE1",
        viewInstanceDefinition.getContextPath());
  }

  @Test
  public void testInstanceData() throws Exception {
    TestUserNameProvider userNameProvider = new TestUserNameProvider("user1");

    ViewInstanceEntity viewInstanceDefinition = getViewInstanceEntity(userNameProvider);

    viewInstanceDefinition.putInstanceData("key1", "foo");

    ViewInstanceDataEntity dataEntity = viewInstanceDefinition.getInstanceData("key1");

    Assert.assertNotNull(dataEntity);

    Assert.assertEquals("foo", dataEntity.getValue());
    Assert.assertEquals("user1", dataEntity.getUser());

    viewInstanceDefinition.putInstanceData("key2", "bar");
    viewInstanceDefinition.putInstanceData("key3", "baz");
    viewInstanceDefinition.putInstanceData("key4", "monkey");
    viewInstanceDefinition.putInstanceData("key5", "runner");

    Map<String, String> dataMap = viewInstanceDefinition.getInstanceDataMap();

    Assert.assertEquals(5, dataMap.size());

    Assert.assertEquals("foo", dataMap.get("key1"));
    Assert.assertEquals("bar", dataMap.get("key2"));
    Assert.assertEquals("baz", dataMap.get("key3"));
    Assert.assertEquals("monkey", dataMap.get("key4"));
    Assert.assertEquals("runner", dataMap.get("key5"));

    viewInstanceDefinition.removeInstanceData("key3");
    dataMap = viewInstanceDefinition.getInstanceDataMap();
    Assert.assertEquals(4, dataMap.size());
    Assert.assertFalse(dataMap.containsKey("key3"));

    userNameProvider.setUser("user2");

    dataMap = viewInstanceDefinition.getInstanceDataMap();
    Assert.assertTrue(dataMap.isEmpty());

    viewInstanceDefinition.putInstanceData("key1", "aaa");
    viewInstanceDefinition.putInstanceData("key2", "bbb");
    viewInstanceDefinition.putInstanceData("key3", "ccc");

    dataMap = viewInstanceDefinition.getInstanceDataMap();

    Assert.assertEquals(3, dataMap.size());

    Assert.assertEquals("aaa", dataMap.get("key1"));
    Assert.assertEquals("bbb", dataMap.get("key2"));
    Assert.assertEquals("ccc", dataMap.get("key3"));

    userNameProvider.setUser("user1");

    dataMap = viewInstanceDefinition.getInstanceDataMap();
    Assert.assertEquals(4, dataMap.size());

    Assert.assertEquals("foo", dataMap.get("key1"));
    Assert.assertEquals("bar", dataMap.get("key2"));
    Assert.assertNull(dataMap.get("key3"));
    Assert.assertEquals("monkey", dataMap.get("key4"));
    Assert.assertEquals("runner", dataMap.get("key5"));
  }

  public static ViewInstanceEntity getViewInstanceEntity() throws Exception {
    InstanceConfig instanceConfig = InstanceConfigTest.getInstanceConfigs().get(0);
    ViewEntity viewDefinition = ViewEntityTest.getViewEntity();
    return new ViewInstanceEntity(viewDefinition, instanceConfig);
  }

  public static ViewInstanceEntity getViewInstanceEntity(ViewInstanceEntity.UserNameProvider userNameProvider)
      throws Exception {
    ViewInstanceEntity viewInstanceEntity = getViewInstanceEntity();
    viewInstanceEntity.setUserNameProvider(userNameProvider);
    return viewInstanceEntity;
  }

  protected static class TestUserNameProvider extends ViewInstanceEntity.UserNameProvider {

    private String user;

    public TestUserNameProvider(String user) {
      this.user = user;
    }

    public void setUser(String user) {
      this.user = user;
    }

    @Override
    public String getUsername() {
      return user;
    }
  }
}
