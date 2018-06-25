## OpenOffice 服务搭建
### 安装步骤

1. 下载 rpm 包 ： 官网： https://www.openoffice.org/download/

2. 解压，进入 /zh-CN/RPMS/ ， 安装 rpm 包： `rpm -ivh *.rpm`

3. 安装完成后，生成 desktop-integration 目录，进入，因为我的系统是 centos 的 ，我选择安装 `rpm -ivh openoffice4.1.5-redhat-menus-4.1.5-9789.noarch.rpm`

4. 安装完成后，目录在 /opt/openoffice4 下
    启动： `/opt/openoffice4/program/soffice -headless -accept="socket,host=0.0.0.0,port=8100;urp;" -nofirststartwizard &`


### 遇到的问题
1. libXext.so.6: cannot open shared object file: No such file or directory
    解决 ： `yum install libXext.x86_64`

2. no suitable windowing system found, exiting.
    解决： `yum groupinstall "X Window System"`

之后再启动，查看监听端口 `netstat -lnp |grep 8100`
已经可以了。


## LibreOffice 服务搭建
### 安装步骤

1. 下载 Linux系统下的 rpm 安装包

2. 将安装包解压缩到目录下

3. 安装
   $ sudo yum install ./RPMS/*.rpm  /* 安装主安装程序的所有rpm包 */
   $ sudo yum install ./RPMS/*.rpm  /* 安装中文语言包中的所有rpm包 */
   $ sudo yum install ./RPMS/*.rpm  /* 安装中文离线帮助文件中的所有rpm包 */

4. 卸载
    $ sudo apt-get remove --purge libreoffice6.x-*  /* 移除所有类似libreoffice6.x-*的包。--purge表示卸载的同时移除所有相关的配置文件 */
    
LibreOffice 的安装表示没有像 OpenOffice 那样遇到很多问题。