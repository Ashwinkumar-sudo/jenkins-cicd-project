
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
                sh '''
                    PREVIOUS_VERSION=$(cat /var/lib/jenkins/deployed-version)
                    NEW_VERSION="${BUILD_NUMBER}"

                    echo "Previous version: ${PREVIOUS_VERSION}"
                    echo "Deploying version: ${NEW_VERSION}"

                    if ! docker image inspect "jenkins-cicd-app:${PREVIOUS_VERSION}" >/dev/null 2>&1; then
                        echo "Previous image not found. Cannot rollback."
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

                    docker run -d \
                        --name jenkins-cicd-app \
                        --restart unless-stopped \
                        -p 80:8080 \
                        "jenkins-cicd-app:${PREVIOUS_VERSION}"

                    for i in $(seq 1 10); do
                        if curl -fsS http://localhost/ >/dev/null; then
                            echo "Rollback successful: ${PREVIOUS_VERSION}"
                            exit 1
                        fi
                        sleep 3
                    done

                    echo "ERROR: Rollback health check failed."
                    exit 1
                '''
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
