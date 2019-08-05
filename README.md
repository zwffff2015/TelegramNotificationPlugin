Jenkins Telegram通知插件

#生成Messages.java
mvn compile

#运行Jenkins，对插件进行测试
mvn hpi:run

#打包插件
mvn package

#上传插件
1. 将hpi文件放到Jenkins的Plugins目录下
2. 将hpi文件，通过Jenkins的插件管理上传安装

插件的详细编写教程，请查阅[实战Jenkins插件开发 - Telegram通知插件](https://www.jianshu.com/p/ce8b65aa197b)
