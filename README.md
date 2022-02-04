# IMDB Search

Test project for learning ElasticSearch. It currently searches the IMDB database for media.

## Tech stack

This project is using Spring Boot and ElasticSearch.

## How to set up

### Elastic setup

This web service needs an ElasticSearch instance at port 9200. For setting up ElasticSearch easily, I recommend using Docker. This command should download an Elastic image if you don't have it and map 9200 port for you:
```
docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 \
 -e "discovery.type=single-node" \
docker.elastic.co/elasticsearch/elasticsearch:7.16.1
```

### Database indexing

First of all, you should download *title.basics.tsv.gz* and *title.ratings.tsv.gz* from the [IMDB Webpage](https://www.imdb.com/interfaces/) and unzip them. Then, run the Spring Boot project. Finally, you only need to run the following command substituting for the file paths you used:
```
curl -XPOST 'localhost:8080/index?path=pathToTitles.tsv&ratingsPath=pathToRatings.tsv'
```
It will take a while for everything to index so please, be patient.

## Api spec
At the moment, there's no Swagger UI server hosted but you can access the up-to-date Api specification (OAS3) [here](doc/openapi.yaml). In the future it will be hosted but for now you can use it copying and pasting it in [Swagger Editor](https://editor.swagger.io).

