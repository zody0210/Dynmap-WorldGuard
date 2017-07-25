pipeline {
  agent any
  stages {
    stage('DakotaMW') {
      steps {
        parallel(
          "DakotaMW": {
            sh 'echo test'
            
          },
          "Import Rating": {
            sh 'echo test'
            
          }
        )
      }
    }
    stage('Duda Sites') {
      steps {
        parallel(
          "Duda Sites": {
            sh 'echo test'
            
          },
          "NPS": {
            echo 'test'
            
          }
        )
      }
    }
  }
}