#!/usr/bin/env bash
systemctl disable es-doc-office.service
systemctl stop es-doc-office.service
rm -rf /usr/lib/systemd/system/es-doc-office-service.service
systemctl daemon-reload
echo "uninstall success!"