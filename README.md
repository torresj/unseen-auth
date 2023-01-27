# unseen-auth

[![torresj](https://circleci.com/gh/torresj/unseen-auth.svg?style=shield)](https://app.circleci.com/pipelines/github/torresj/unseen-auth)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Coverage Status](https://coveralls.io/repos/github/torresj/unseen-auth/badge.svg?branch=main)](https://coveralls.io/github/torresj/unseen-auth?branch=main)

[![CircleCI](https://dl.circleci.com/insights-snapshot/gh/torresj/unseen-auth/main/build_and_deploy/badge.svg?window=30d)](https://app.circleci.com/insights/github/torresj/unseen-auth/workflows/build_and_deploy/overview?branch=main&reporting-window=last-30-days&insights-snapshot=true)

Authentication and Authorization microservice for Unseen project

## Requirements

For building and running the application you need:

- [JDK 17](https://www.oracle.com/java/technologies/downloads/#java17)
- [Maven 3](https://maven.apache.org)

## Running the application locally

There are several ways to run a Spring Boot application on your local machine. One way is to execute the `main` method
in the `com.torresj.unseenauth.UnseenAuthApplication` class from your IDE.

Alternatively you can use
the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html)
like so:

```shell
mvn spring-boot:run
```

This microservice is connected with cloud config server and MariaDB database. If you want to run it locally without
connect it with other services you can use `local` profile.

# Deployment

This microservice can be deployed in [Kubernetes](https://kubernetes.io/) cluster using [Helm](https://helm.sh/).

### Install Helm

Helm is a tool for managing Kubernetes charts. Charts are packages of pre-configured Kubernetes resources.

To install Helm, refer to the [Helm install guide](https://github.com/helm/helm#install) and ensure that the `helm`
binary is in the `PATH` of your shell.

### Install unseen-auth Chart

This repo includes a [Helm folder](https://github.com/torresj/unseen-auth/tree/main/src/main/helm) where we have
the `unseen-auth` chart. All values that can be use in this chart are defined
in [values.yaml](https://github.com/torresj/unseen-auth/blob/main/src/main/helm/unseen-auth/values.yaml).

### Variable substitution

Into [unseen-auth chart folder](https://github.com/torresj/unseen-auth/tree/main/src/main/helm/unseen-auth) Certain
values have been defined dynamically such as the version of the project. When you build the project with `maven`, a new
folder `target` is created and inside a new helm folder is generated with all the variables correctly substituted
in `target/helm/unseen-auth`

## Copyright

Released under the GNU General Public License V3.0. See
the [LICENSE](https://github.com/torresj/unseen-auth/blob/main/LICENSE) file.