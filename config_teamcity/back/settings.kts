import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.notifications
import jetbrains.buildServer.configs.kotlin.buildFeatures.vcsLabeling
import jetbrains.buildServer.configs.kotlin.buildSteps.MavenBuildStep
import jetbrains.buildServer.configs.kotlin.buildSteps.SSHUpload
import jetbrains.buildServer.configs.kotlin.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.buildSteps.sshExec
import jetbrains.buildServer.configs.kotlin.buildSteps.sshUpload
import jetbrains.buildServer.configs.kotlin.projectFeatures.githubConnection
import jetbrains.buildServer.configs.kotlin.triggers.finishBuildTrigger
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2022.10"

project {
    description = "Project for backend"

    vcsRoot(BootcampBackend1)
    vcsRoot(BootcampBackend)

    buildType(DeployToQaEnvironment)
    buildType(BuildAndPackage)
    buildType(DeployDev)

    params {
        password("user11", "credentialsJSON:8131b9fc-b29a-4063-92aa-59d91f0bc514", display = ParameterDisplay.HIDDEN)
        password("user10", "credentialsJSON:d414da0b-2830-48b6-91fb-a701e1e6c1f4", display = ParameterDisplay.HIDDEN)
        password("pass11", "credentialsJSON:12d7994c-1ef5-4cca-973b-fa6fcc9fb44f", display = ParameterDisplay.HIDDEN)
        password("pass10", "credentialsJSON:6b27368f-bc5f-4397-80a2-f7c2aeec6218", display = ParameterDisplay.HIDDEN)
    }

    features {
        githubConnection {
            id = "PROJECT_EXT_5"
            displayName = "GitHub.com"
            clientId = "MigelAlfa"
            clientSecret = "credentialsJSON:a7cddf1b-94e1-4631-bdec-93eb08d1fdc2"
        }
    }
}

object BuildAndPackage : BuildType({
    name = "Build and Package"

    artifactRules = """
        target => target
        back.latest.image.version
    """.trimIndent()
    publishArtifacts = PublishMode.SUCCESSFUL

    vcs {
        root(BootcampBackend1)

        checkoutMode = CheckoutMode.ON_SERVER
    }

    steps {
        script {
            name = "set variables for the tag"
            scriptContent = """
                export current_build_date_format="+%%Y%%m%%d"
                export current_build_date="${'$'}(date ${'$'}current_build_date_format)"
                GIT_HASH=%build.vcs.number%
                GIT_HASH_SHORT="${'$'}(echo ${'$'}GIT_HASH | cut -c1-6)"
                echo "##teamcity[buildNumber '${'$'}current_build_date.${'$'}GIT_HASH_SHORT']"
            """.trimIndent()
            param("org.jfrog.artifactory.selectedDeployableServer.downloadSpecSource", "Job configuration")
            param("org.jfrog.artifactory.selectedDeployableServer.useSpecs", "false")
            param("org.jfrog.artifactory.selectedDeployableServer.uploadSpecSource", "Job configuration")
        }
        maven {
            name = "Maven install"
            goals = "clean install"
            mavenVersion = auto()
            coverageEngine = jacoco {
                classLocations = "target/**/*.class"
            }
            dockerImage = "maven:3.8.6-amazoncorretto-17"
            dockerImagePlatform = MavenBuildStep.ImagePlatform.Linux
            param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
        }
        dockerCommand {
            name = "Build the Docker container"
            executionMode = BuildStep.ExecutionMode.RUN_ON_SUCCESS
            commandType = build {
                source = content {
                    content = """
                        FROM openjdk:17-alpine
                        ARG JAR_FILE=target/*.jar
                        RUN mkdir -p /opt/profiler/photo
                        COPY ${'$'}{JAR_FILE}  /opt/profiler/app.jar
                        ENTRYPOINT ["java","-jar","/opt/profiler/app.jar"]
                    """.trimIndent()
                }
                namesAndTags = """
                    jfrog.it-academy.by/itbc-devops-test/backend:%env.BUILD_NUMBER%
                    jfrog.it-academy.by/itbc-devops-test/backend:latest
                """.trimIndent()
            }
        }
        step {
            name = "Push a container to jfrog"
            type = "ArtifactoryDocker"
            param("org.jfrog.artifactory.selectedDeployableServer.dockerImageName", "jfrog.it-academy.by/itbc-devops-test/backend:%env.BUILD_NUMBER%")
            param("org.jfrog.artifactory.selectedDeployableServer.dockerCommand", "PUSH")
            param("org.jfrog.artifactory.selectedDeployableServer.urlId", "0")
            param("org.jfrog.artifactory.selectedDeployableServer.resolvingRepo", "helm-virtual")
            param("org.jfrog.artifactory.selectedDeployableServer.targetRepo", "itbc-devops-test")
        }
        step {
            name = "Push a container:latest to jfrog"
            type = "ArtifactoryDocker"
            param("org.jfrog.artifactory.selectedDeployableServer.dockerImageName", "jfrog.it-academy.by/itbc-devops-test/backend:latest")
            param("org.jfrog.artifactory.selectedDeployableServer.dockerCommand", "PUSH")
            param("org.jfrog.artifactory.selectedDeployableServer.urlId", "0")
            param("org.jfrog.artifactory.selectedDeployableServer.resolvingRepo", "helm-virtual")
            param("org.jfrog.artifactory.selectedDeployableServer.targetRepo", "itbc-devops-test")
        }
        script {
            name = "Save image version"
            scriptContent = "echo %env.BUILD_NUMBER% > back.latest.image.version"
            param("org.jfrog.artifactory.selectedDeployableServer.downloadSpecSource", "Job configuration")
            param("org.jfrog.artifactory.selectedDeployableServer.useSpecs", "false")
            param("org.jfrog.artifactory.selectedDeployableServer.uploadSpecSource", "Job configuration")
        }
    }

    triggers {
        vcs {
            branchFilter = "+:refs/heads/develop"
        }
    }

    features {
        notifications {
            notifierSettings = slackNotifier {
                connection = "PROJECT_EXT_3"
                sendTo = "#profiler2022"
                messageFormat = verboseMessageFormat {
                    addBranch = true
                    addChanges = true
                    addStatusText = true
                    maximumNumberOfChanges = 10
                }
            }
            buildStarted = true
            buildFailedToStart = true
            buildFailed = true
            buildFinishedSuccessfully = true
            firstBuildErrorOccurs = true
            buildProbablyHanging = true
            queuedBuildRequiresApproval = true
        }
        vcsLabeling {
            vcsRootId = "${BootcampBackend1.id}"
            labelingPattern = "v.%build.number%"
            successfulOnly = true
        }
    }
})

object DeployDev : BuildType({
    name = "Deploy to Dev environment"

    enablePersonalBuilds = false
    type = BuildTypeSettings.Type.DEPLOYMENT
    maxRunningBuilds = 1

    vcs {
        root(BootcampBackend)

        checkoutMode = CheckoutMode.ON_SERVER
    }

    steps {
        sshExec {
            name = "Docker run"
            commands = """
                docker kill backend
                docker rm backend
                docker rmi jfrog.it-academy.by/itbc-devops-test/backend:latest
                docker run -d -e  CORS_ALLOWED_ORIGINS="*" \
                -e CORS_ALLOWED_METHODS="*" \
                -e DATABASE_URL='jdbc:mysql://192.168.205.10:3306/dev' \
                -e DATABASE_USERNAME='%user10%' \
                -e DATABASE_PASSWORD='%pass10%' \
                -e SPRING_PROFILES_ACTIVE='dev' \
                -e JWT_SECRET_PHRASE='profile' \
                -e IMAGES_STORAGE_FOLDER='/opt/profiler/photo' \
                -p 8080:8080 \
                -v /srv/photo:/opt/profiler/photo \
                --log-driver=loki --log-opt loki-url="http://192.168.205.10:3100/loki/api/v1/push" \
                --name backend jfrog.it-academy.by/itbc-devops-test/backend:latest
            """.trimIndent()
            targetUrl = "192.168.205.10"
            authMethod = password {
                username = "user"
                password = "credentialsJSON:3e12d360-0f6a-43b8-b4c6-a773c4d20a9a"
            }
        }
        sshExec {
            name = "Docker compose up"
            enabled = false
            commands = "docker compose up -d"
            targetUrl = "192.168.205.10"
            authMethod = password {
                username = "user"
                password = "credentialsJSON:3e12d360-0f6a-43b8-b4c6-a773c4d20a9a"
            }
        }
        sshExec {
            name = "Сheck if a docker instance is running"
            commands = """
                #!/bin/bash
                #Сheck if a docker instance is running
                sleep 15s
                
                if docker ps | grep -q backend
                then 
                    echo "Running!"
                else
                    echo "Not running!"
                    exit 1
                fi
            """.trimIndent()
            targetUrl = "192.168.205.10"
            authMethod = password {
                username = "user"
                password = "credentialsJSON:3e12d360-0f6a-43b8-b4c6-a773c4d20a9a"
            }
        }
    }

    triggers {
        finishBuildTrigger {
            buildType = "${BuildAndPackage.id}"
            successfulOnly = true
            branchFilter = ""
        }
    }

    features {
        notifications {
            notifierSettings = slackNotifier {
                connection = "PROJECT_EXT_3"
                sendTo = "#profiler2022"
                messageFormat = verboseMessageFormat {
                    addStatusText = true
                    maximumNumberOfChanges = 10
                }
            }
            buildStarted = true
            buildFailedToStart = true
            buildFailed = true
            buildFinishedSuccessfully = true
            firstBuildErrorOccurs = true
            buildProbablyHanging = true
            queuedBuildRequiresApproval = true
        }
    }
})

object DeployToQaEnvironment : BuildType({
    name = "Deploy to QA environment"

    artifactRules = "back.image.version.inQA"
    publishArtifacts = PublishMode.SUCCESSFUL

    steps {
        sshUpload {
            name = "Copy file to remote server"
            transportProtocol = SSHUpload.TransportProtocol.SCP
            sourcePath = "back.latest.image.version"
            targetUrl = "192.168.205.11"
            authMethod = password {
                username = "user"
                password = "credentialsJSON:3e12d360-0f6a-43b8-b4c6-a773c4d20a9a"
            }
        }
        sshExec {
            name = "Docker run"
            commands = """
                docker kill backend
                docker rm backend
                docker rmi jfrog.it-academy.by/itbc-devops-test/backend:latest
                version_to_deploy=${'$'}(cat back.latest.image.version)
                docker run -d -e CORS_ALLOWED_ORIGINS="*" \
                -e CORS_ALLOWED_METHODS="*" \
                -e DATABASE_URL='jdbc:mysql://192.168.205.11:3306/dev' \
                -e DATABASE_USERNAME='%user11%' \
                -e DATABASE_PASSWORD='%pass11%' \
                -e SPRING_PROFILES_ACTIVE='qa' \
                -e JWT_SECRET_PHRASE='profile' \
                -e IMAGES_STORAGE_FOLDER='/opt/profiler/photo' \
                -p 8080:8080 \
                -v /srv/photo:/opt/profiler/photo \
                --name backend jfrog.it-academy.by/itbc-devops-test/backend:${'$'}{version_to_deploy}
            """.trimIndent()
            targetUrl = "192.168.205.11"
            authMethod = password {
                username = "user"
                password = "credentialsJSON:3e12d360-0f6a-43b8-b4c6-a773c4d20a9a"
            }
        }
        script {
            name = "Save image version"
            scriptContent = "cat back.latest.image.version > back.image.version.inQA"
            param("org.jfrog.artifactory.selectedDeployableServer.downloadSpecSource", "Job configuration")
            param("org.jfrog.artifactory.selectedDeployableServer.useSpecs", "false")
            param("org.jfrog.artifactory.selectedDeployableServer.uploadSpecSource", "Job configuration")
        }
        sshExec {
            name = "Сheck if a docker instance is running"
            commands = """
                #!/bin/bash
                sleep 15s
                
                if docker ps | grep -q backend
                then 
                    echo "Running!"
                else
                    echo "Not running!"
                    exit 1
                fi
            """.trimIndent()
            targetUrl = "192.168.205.11"
            authMethod = password {
                username = "user"
                password = "credentialsJSON:3e12d360-0f6a-43b8-b4c6-a773c4d20a9a"
            }
        }
    }

    features {
        notifications {
            notifierSettings = slackNotifier {
                connection = "PROJECT_EXT_3"
                sendTo = "#profiler2022"
                messageFormat = verboseMessageFormat {
                    addStatusText = true
                    maximumNumberOfChanges = 10
                }
            }
            buildStarted = true
            buildFailedToStart = true
            buildFailed = true
            buildFinishedSuccessfully = true
            firstBuildErrorOccurs = true
            buildProbablyHanging = true
            queuedBuildRequiresApproval = true
        }
    }

    dependencies {
        artifacts(BuildAndPackage) {
            buildRule = lastSuccessful()
            artifactRules = "back.latest.image.version"
        }
    }
})

object BootcampBackend : GitVcsRoot({
    name = "bootcamp/backend"
    url = "https://git.it-academy.by/bootcamp/dev.git"
    branch = "refs/heads/develop"
    branchSpec = "+:refs/heads/develop"
    authMethod = password {
        password = "credentialsJSON:237913f4-bed1-4f9f-bbc1-efe4416ac43e"
    }
})

object BootcampBackend1 : GitVcsRoot({
    name = "bootcamp/backend (1)"
    url = "https://git.it-academy.by/bootcamp/dev.git"
    branch = "refs/heads/develop"
    branchSpec = "+:refs/heads/develop"
    authMethod = password {
        password = "credentialsJSON:237913f4-bed1-4f9f-bbc1-efe4416ac43e"
    }
})
