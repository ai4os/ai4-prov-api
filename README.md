# ai4eosc-prov-api

Provenance API is the core component of the provenance service within the AI4OS-HUB platform. The platform collects all provenance information related to a module or project and stores it in a postgreSQL database using a structured format based on JSON fragments.

When provenance data is requested in RDF format, the platform reconstructs the provenance RDF  by applying the rules defined in a RML (RDF Mapping Language) file (executed by the RML engine, carml) over the stored JSON fragments.

## API BASIC Endpoints

#### POST /meta-data

Uploads meta-data of a module to the provenance database.

Body of the HTTP POST
```
{
   "sources": {
    "applicationId": "<application-id>",
    "jenkinsWorkflow": {
        "name": "<application-id (in jenkins)>",
        "group": "<jenkins-group>",
        "branch": "<jenkins-branch>",
        "build": <jenkins-build-number>
    }
   },
   "credentials": {
        ...
   }
}
```

In the body you can specify
* applicationId: the application you want to fetch metadata from
* jenkinsWorkflow: the jenkins workflow of the application
    * (name, group, branch, build): fields to select which workflow of jenkins you want to select
* credentials, in case you're application have some metadata stored in a service with restricted access. you have to write here the credentials that provenance api will use for login in to that service. For example for MLFlow, you will have to write:
```
{
    ...

   "credentials": {
       "mlflow": {
           "username": "<mlflow-user>",
           "password": "<mlflow-password>"
       }
   }
}
```

Provenance API can access to `https://mlflow.cloud.ai4eosc.eu` and to `http://mlflow.cloud.imagine-ai.eu` as a fallback if the first domain fails. So the credentials user have to be a user of one of these severs.

Once you send this request, Provenance API will search inside the `ai4-metadata.yml`of the application/module github repository and if it finds some services ids like nomad jobs or mlflows runs, It will add them to the metadata JSON along with the Jenkins and the application metadata (`metadata.json` file).

### GET /rdf?applicationId=<applicationId>

Generates the RDF provenance graph of the application built from the metadata fetched by the last `POST /meta-data` of the application.

## What is Provenance API used for inside AI4EOSC?

Provenance API is used along with Jenkins pipelines to generate a provenance RDF for every single module in the AI4EOSC platform.
 

You can take a look to the provenance graph (visual representation of provenance RDF) of an application through AI4EOSC marketplace.