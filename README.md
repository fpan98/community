# 仿牛客论坛-项目说明
## 1、项目简介
演示地址：http://1.13.15.89 
由于购买的服务器内存只有2G，项目中的部分服务暂时不可以访问，选择性的把elasticsearch服务关闭了。  
该论坛是一个互动交流平台，实现了注册登录、发帖评论、回复点赞、消息提醒、内容搜索和网站数据统计的功能，主要技术栈包括：Spring Boot、Mybatis、Redis、Kafka、Elasticsearch。  
  
工作内容：  
+ 使用Redis存储验证码和登录ticket，解决分布式session问题；   
+ 使用Redis的set实现点赞，zset实现关注，HyperLogLog统计UV，Bitmap统计DAU；  
+ 使用kafka处理发送评论、点赞和关注等系统通知，起到解耦和异步调用的作用；   
+ 使用Elasticsearch实现对帖子的搜索功能，通过IK中文分词器增加增量索引和全局索引，实现搜索关键词高亮显示等功能；  
+ 对热贴排行模块，使用分布式缓存Redis和本地缓存Caffeine作为多级缓存，将QPS提升60多倍(4.3-285)，大大提升了网站访问速度，并使用Quartz定时更新热贴排行榜。  
## 2、配置文件
根据自身情况修改application-develop.yaml中的部分配置，包括：
+ 数据库的用户和密码
+ 项目的domain
+ 用于发送验证的邮箱用户和密码
+ 七牛云的access、secret和url
## 3、项目部署
> 写在前面，最起码要选用4G内存的服务器，否则项目启动不起来，即使你限制修改启动内存后，部分服务包括kafka、elasticsearch也一定会存在问题.
+ jdk 1.8
+ maven 3.8.4
+ mysql 8.0.27
+ redis 6.2.6
+ kafka 3.1.0
+ elasticsearch 7.15.2
+ tomcat 9.0.62
+ nginx 1.14.1
## 4、项目总结
该项目也将是我校招时唯一的java项目了，学完之后进行了如下总结：
![社区论坛项目总结](https://raw.githubusercontent.com/fpan98/community/main/images/%E8%AE%BA%E5%9D%9B%E9%A1%B9%E7%9B%AE.png)