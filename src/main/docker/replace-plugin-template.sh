#!/usr/bin/env sh

envsubst '$PLUGIN_REPOSITORY_HOST' < /usr/share/nginx/html/updatePlugins-template.xml > /usr/share/nginx/html/updatePlugins.xml
