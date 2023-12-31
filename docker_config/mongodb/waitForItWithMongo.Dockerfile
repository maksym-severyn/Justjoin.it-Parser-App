FROM mongo:7.0.4

COPY wait-for-it.sh /

ENTRYPOINT ["docker-entrypoint.sh"]

EXPOSE 27017
CMD ["mongod"]