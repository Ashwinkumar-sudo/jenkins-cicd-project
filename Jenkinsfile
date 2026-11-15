
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

        stage('Deploy') {
            steps {
                sh '''
                    docker rm -f jenkins-cicd-app || true

                    docker run -d \
                    --name jenkins-cicd-app \
                    --restart unless-stopped \
                    -p 80:8080 \
                    jenkins-cicd-app:${BUILD_NUMBER}
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
