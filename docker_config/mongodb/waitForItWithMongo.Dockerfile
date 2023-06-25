FROM mongo:latest

COPY wait-for-it.sh /

ENTRYPOINT ["docker-entrypoint.sh"]

EXPOSE 27017
CMD ["mongod"]