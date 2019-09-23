/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

export default {
  common: {
    menuData: [{
      title: 'Data governance',
      child: [{
        title: 'Config regist',
        href: '/config-regist'
      }, {
        title: 'Config manage',
        href: '/config-manage'
      }, {
        title: 'Orchestration',
        href: '/orchestration'
      }]
    }],
    connected: 'Connected',
    connection: 'Connection',
    del: 'Delete',
    notify: {
      title: 'Prompt',
      conSucMessage: 'Connection succeeded',
      conFailMessage: 'Connection failed',
      delSucMessage: 'Delete succeeded',
      delFailMessage: 'Delete failed',
      updateCompletedMessage: 'Update completed',
      updateFaildMessage: 'Update faild'
    },
    loginOut: 'Sign out'
  },
  login: {
    btnTxt: 'Login',
    labelUserName: 'Username',
    labelPassword: 'Password'
  },
  btn: {
    submit: 'Submit',
    reset: 'Reset',
    cancel: 'Cancel'
  },
  input: {
    pUserName: 'Please enter user name',
    pPaasword: 'Please enter your password'
  },
  index: {
    btnTxt: 'ADD',
    registDialog: {
      title: 'Add a registry',
      name: 'name',
      centerType: 'centerType',
      address: 'address',
      orchestrationName: 'orchestrationName',
      namespaces: 'Namespaces',
      digest: 'Digest',
      btnConfirmTxt: 'Confirm',
      btnCancelTxt: 'Cancel'
    },
    table: {
      operate: 'operate'
    },
    rules: {
      name: 'Please enter the name of the registration center',
      address: 'Please enter the registration center address',
      namespaces: 'Please enter a namespace',
      centerType: 'Please select a centerType',
      orchestrationName: 'Please enter a orchestrationName',
      digest: 'Please enter a digest'
    }
  },
  orchestration: {
    serviceNode: 'Service node',
    slaveDataSourceName: 'Slave DataSource Info',
    dataSource: {
      schema: 'Schema',
      masterDataSourceName: 'Master DataSource Name',
      slaveDataSourceName: 'Slave DataSource Name'
    },
    instance: {
      instanceId: 'Instance Id',
      serverIp: 'Server Ip'
    }
  },
  configManage: {
    schemaRules: {
      name: 'Please enter the name of the schema',
      ruleConfig: 'Please enter the rule config of the schema',
      dataSourceConfig: 'Please enter the data source config of the schema'
    }
  }
}
