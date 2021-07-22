/**
 * This pipeline will build and deploy a Docker image with Kaniko
 * https://github.com/GoogleContainerTools/kaniko
 * without needing a Docker host
 *
 * You need to create a jenkins-docker-cfg secret with your docker config
 * as described in
 * https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/#create-a-secret-in-the-cluster-that-holds-your-authorization-token
 */

podTemplate(yaml: '''
              kind: Pod
              spec:
                containers:
                - name: kaniko
                  image: gcr.io/kaniko-project/executor:v1.6.0-debug
                  imagePullPolicy: Always
                  command:
                  - sleep
                  args:
                  - 99d
                  volumeMounts:
                  - name: gcp-sa-secret
                    mountPath: /secret
                  env:
                  - name: GOOGLE_APPLICATION_CREDENTIALS
                    value: /secret/rak-card-dev-92cb5b0e01ea.json
                restartPolicy: Never
                volumes:
                - name: gcp-sa-secret
                  secret:
                    secretName: gcp-sa-secret
'''
  ) {

  node(POD_LABEL) {
    stage('Build with Kaniko') {
      git 'https://github.com/jenkinsci/docker-inbound-agent.git'
      container('kaniko') {
        sh '/kaniko/executor -f `pwd`/Dockerfile -c `pwd` --insecure --skip-tls-verify --cache=true --destination=mydockerregistry:5000/myorg/myimage'
      }
    }
  }
}
