pipeline {
    agent any

    triggers {
        // Poll SCM every 5 minutes to check for git updates
        pollSCM('*/5 * * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/Vichada/DevOps-Final.git
            }
        }

        stage('Build & Test') {
            steps {
                // Execute the Maven build and run tests targeting the SQLite profile
                bat 'mvn clean package test -Dspring.profiles.active=test'
            }
        }

        stage('Deploy') {
            steps {
                // Trigger the Ansible playbook to deploy to the web server
                bat 'ansible-playbook playbook.yml'
            }
        }
    }

    post {
        failure {
            // Email notifications sent on build failure
            mail to: 'developer@example.com',
                 cc: 'srengty@gmail.com',
                 subject: "Build Error in Jenkins: ${currentBuild.fullDisplayName}",
                 body: "The build failed. Please check the console log outputs."
        }
    }
}
