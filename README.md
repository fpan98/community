# 仿牛客论坛-项目说明
## 1、配置文件
根据自身情况修改application-develop.yaml中的部分配置，包括：
+ 数据库的用户和密码
+ 项目的domain
+ 用于发送验证的邮箱用户和密码
+ 七牛云的access、secret和url
## 2、项目部署
> 写在前面，最起码要选用4G内存的服务器，否则项目启动不起来，及时你限制修改启动内存后，部分服务包括kafka、elasticsearch也一定会存在问题.
+ jdk 1.8
+ maven 3.8.4
+ mysql 8.0.27
+ kafka 3.1.0
+ elasticsearch 7.15.2
+ tomcat 9.0.62
+ nginx 1.14.1
## 3、项目总结
该项目也将是我校招时唯一的java项目了，学完之后进行了如下总结：
![社区论坛项目总结](https://raw.githubusercontent.com/fpan98/community/main/images/%E8%AE%BA%E5%9D%9B%E9%A1%B9%E7%9B%AE.png)