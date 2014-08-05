#
# Cookbook Name:: cdap
# Recipe:: master
#
# Copyright (C) 2013-2014 Continuuity, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

include_recipe 'java::default'
include_recipe 'cdap::default'

package 'cdap-master' do
  action :install
end

if node['cdap'].key?('security') && node['cdap']['security'].key?('cdap_keytab') &&
  node['cdap']['security'].key?('cdap_principal')
  my_vars = { :options => node['cdap']['security'] }

  directory '/etc/default' do
    owner 'root'
    group 'root'
    mode '0755'
    action :create
  end

  template '/etc/default/cdap-master' do
    source 'generic-env.sh.erb'
    mode '0755'
    owner 'root'
    group 'root'
    action :create
    variables my_vars
  end # End /etc/default/cdap-master

  package 'kstart'
  group 'hadoop' do
    append true
    members [ 'cdap' ]
    action :modify
  end
end

service 'cdap-master' do
  status_command 'service cdap-master status'
  action :nothing
end
