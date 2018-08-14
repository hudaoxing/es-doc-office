#!/usr/bin/env bash

SERVICE_NAME="es-doc-office"
WORK_DIR=$(dirname "$PWD")
JAVA_HOME="$JAVA_HOME"
if [ -z "$JAVA_HOME" ]; then
  echo "No JDK found. Please validate JAVA_HOME environment variable points to valid JDK installation."
  exit 1
fi
echo jdk path is :${JAVA_HOME}

cat>/usr/lib/systemd/system/${SERVICE_NAME}.service<<EOF
 [Unit]
 Description=${SERVICE_NAME} daemon service
 After=network.target

 [Service]
 ## 可以包含的值为simple、forking、oneshot、dbus、notify、idel其中之一。
 ## Type=forking

 ## 守护进程的PID文件，必须是绝对路径，强烈建议在Type=forking的情况下明确设置此选项
 ## PIDFile=/project/frp_0.19.0_linux_386

 ## 设置启动服务是要执行的命令（命令+参数）
 ExecStart=${JAVA_HOME}/bin/java -jar ${WORK_DIR}/boot/${SERVICE_NAME}.jar
 ## 另外，143 是spring-boot服务被stop的时候的status code
 ## 如果不加上SuccessExitStatus=143，stop服务的时候会变成failed状态，而不是inactive状态
 SuccessExitStatus=143

 ## 当服务进程正常退出、异常退出、被杀死、超时的时候，是否重启系统该服务。进程通过正常操作被停止则不会被执行重启。可选值为：
 ## no：默认值，表示任何时候都不会被重启
 ## always：表示会被无条件重启
 ## no-success：表示仅在服务进程正常退出时重启
 ## on-failure：表示仅在服务进程异常退出时重启
 ## 所谓正常退出是指，退出码为“0”，或者到IGHUP, SIGINT, SIGTERM, SIGPIPE 信号之一，并且退出码符合 SuccessExitStatus= 的设置。
 ## 所谓异常退出时指，退出码不为“0”，或者被强杀或者因为超时被杀死。
 Restart=on-abort


 [Install]
 WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable es-doc-office.service
echo "install success!"