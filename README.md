# ai4eosc-prov-api

Provenance API is the core component of the provenance service within the AI4OS-HUB platform. The platform collects all provenance information related to a module or project and stores it in a postgreSQL database using a structured format based on JSON fragments.

When provenance data is requested in RDF format, the platform reconstructs the RDF provenance by applying predefined RML (RDF Mapping Language) file (executed by the RML engine, carml) to the stored JSON fragments.

## Project/module provenance

In order to get the provenance of a project.

First, you will need to send an HTTP POST to /metadata with a JSON in the request body. This will warn the provenance service, so it will gather all the metadata and save it in database.

Structure of the JSON.
```
{
    "applicationId": "<module-name>",
    "mlflowExperiment": {
        "experimentId": "<mlflow-experiment-id>"
    },
    "jenkinsWorkflow": {
        "jobName": "<jenkins-job-name/module-name>",
        "jobGroup": "<jenkins-job-group>",
        "jobBranch": "<jenkins-branch>",
        "execution": <build-number>
    }
}
```

E.g of ai4os-yolov8-torch project
```
{
    "applicationId": "ai4os-yolov8-torch",
    "mlflowExperiment": {
        "experimentId": "yolov8_L"
    },
    "jenkinsWorkflow": {
        "jobName": "ai4os-yolov8-torch",
        "jobGroup": "AI4OS-hub",
        "jobBranch": "main",
        "execution": 46
    }
}
```

The server will respond with a "metadata saved" message. After that, any HTTP GET request to `/provenance?applicationId=<module-name>`—where <module-name> matches the value provided in the applicationId field of your JSON fragment—will return the corresponding provenance in RDF format in the response body.

In the future, this process will be carried out automatically each time a Jenkins pipeline is executed. Jenkins will be responsible for sending the request to the provenance service using the appropriate IDs, which will be specified in a configuration file within each module/project repository.

There are some features that are in development such as nomad metadata or harbor metadata, if you want to use this features contact the author, castrop@predictia.es.
