
pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                sh 'mvn -B clean test package'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh '''
                    docker build \
                    -t jenkins-cicd-app:${BUILD_NUMBER} \
                    -t jenkins-cicd-app:latest .
                '''
            }
        }

        stage('Deploy with Automatic Rollback') {
            steps {
                script {

                    def deployStatus = sh(
                        script: '''
                            PREVIOUS_VERSION=$(cat /var/lib/jenkins/deployed-version)
                            NEW_VERSION="${BUILD_NUMBER}"

                            echo "Previous version: ${PREVIOUS_VERSION}"
                            echo "Deploying version: ${NEW_VERSION}"

                            if [ -z "$PREVIOUS_VERSION" ]; then
                                echo "ERROR: Previous version is missing."
                                exit 1
                            fi

                            if ! docker image inspect "jenkins-cicd-app:${PREVIOUS_VERSION}" >/dev/null 2>&1; then
                                echo "ERROR: Previous Docker image not found."
                                exit 1
                            fi

                            docker rm -f jenkins-cicd-app || true

                            if docker run -d \
                                --name jenkins-cicd-app \
                                --restart unless-stopped \
                                -p 80:8080 \
                                "jenkins-cicd-app:${NEW_VERSION}"; then

                                for i in $(seq 1 10); do
                                    if curl -fsS http://localhost/ >/dev/null; then
                                        echo "${NEW_VERSION}" > /var/lib/jenkins/deployed-version
                                        echo "Deployment successful: ${NEW_VERSION}"
                                        exit 0
                                    fi
                                    sleep 3
                                done
                            fi

                            echo "Deployment failed. Rolling back..."

                            docker rm -f jenkins-cicd-app || true

                            if ! docker run -d \
                                --name jenkins-cicd-app \
                                --restart unless-stopped \
                                -p 80:8080 \
                                "jenkins-cicd-app:${PREVIOUS_VERSION}"; then

                                echo "ERROR: Could not start previous container."
                                exit 1
                            fi

                            for i in $(seq 1 10); do
                                if curl -fsS http://localhost/ >/dev/null; then
                                    echo "${PREVIOUS_VERSION}" > /var/lib/jenkins/deployed-version
                                    echo "Rollback successful: ${PREVIOUS_VERSION}"
                                    exit 2
                                fi
                                sleep 3
                            done

                            echo "ERROR: Rollback health check failed."
                            exit 1
                        ''',
                        returnStatus: true
                    )

                    if (deployStatus == 0) {
                        echo 'New deployment completed successfully.'
                    }
                    else if (deployStatus == 2) {
                        unstable('New deployment failed, but rollback succeeded. Previous application is running.')
                    }
                    else {
                        error('Deployment and/or rollback failed. Check the console output.')
                    }
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true,
                  testResults: 'target/surefire-reports/*.xml'
        }
    }
}
