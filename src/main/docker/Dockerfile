FROM nginx:1.29.1-alpine

EXPOSE 80

COPY build/libs/citrusframework-intellij-plugin-?.?.?.jar /usr/share/nginx/html/citrus-intellij-platform-plugin.jar
COPY build/docker/updatePlugins-template.xml /usr/share/nginx/html/updatePlugins-template.xml
ADD --chmod=+x src/main/docker/replace-plugin-template.sh /docker-entrypoint.d/replace-plugin-template.sh
