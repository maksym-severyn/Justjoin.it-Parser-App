FROM mongo:7.0.5

COPY wait-for-it.sh /

ENTRYPOINT ["docker-entrypoint.sh"]

EXPOSE 27017
CMD ["mongod"]