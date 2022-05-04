# 仿牛客论坛-项目使用说明

## 1、配置文件
根据自身情况修改application-develop.yaml中的部分配置，包括：
+ 数据库的用户和密码
+ 项目的domain
+ 用于发送验证的邮箱用户和密码
+ 七牛云的access、secret和url

## 2、项目部署
> 写在前面，最起码要选用4G内存的服务器，否则项目启动不起来，及时你限制修改启动内存后，部分服务包括kafka、elasticsearch也一定会存在问题.
+ jdk

+ maven

+ mysql

+ kafka

+ elasticsearch

+ tomcat

+ nginx