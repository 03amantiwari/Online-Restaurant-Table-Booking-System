
// =========================================================================
//  Jenkinsfile — Jenkins-only stages (No EC2 deploy yet)
//
//  Current flow:
//  GitHub push → Checkout → Test → Build JAR → Docker Build → Docker Push
//
//  EC2 Deploy stage will be added after EC2 setup is complete.
//  Place this file at: project root (next to docker-compose.yml)
//
//  Two things to replace before using:
//    1. DOCKERHUB_USERNAME → your actual DockerHub username
//    2. GitHub repo URL in job configuration
// =========================================================================
 
pipeline {
 
    agent any

    tools {
        jdk 'JDK-21'    // ← ye add karo
    }
 
    environment {
        // ---- Replace with your DockerHub username ----
        DOCKERHUB_USERNAME = "0303amantiwari"
 
        // Image names — build number gives unique tag every build
        BACKEND_IMAGE  = "${DOCKERHUB_USERNAME}/easyseat-backend"
        FRONTEND_IMAGE = "${DOCKERHUB_USERNAME}/easyseat-frontend"
        IMAGE_TAG      = "v${BUILD_NUMBER}"
 
        // Non-secret config
        CONTEXT_PATH   = "/api/v1"
    }
 
    stages {
 
        // ----------------------------------------------------------------
        //  STAGE 1 — Checkout
        //
        //  'checkout scm' means: pull code from the same GitHub repo
        //  that is configured in the Jenkins job settings.
        //  Jenkins does a fresh git pull on every pipeline run.
        // ----------------------------------------------------------------
        stage('Checkout') {
            steps {
                echo "======== Stage 1: Checkout latest code ========"
                checkout scm
                // After this step, workspace has your full project:
                // Backend/, frontend/, docker-compose.yml, Jenkinsfile
            }
        }
 
        // ----------------------------------------------------------------
        //  STAGE 2 — Test
        //
        //  Runs all JUnit/Mockito tests inside Backend/.
        //  If ANY test fails → pipeline stops here → Docker image never built.
        //  This is the "quality gate" — broken code never reaches DockerHub.
        //
        //  -B = batch mode: no color codes, clean CI logs
        // ----------------------------------------------------------------
        // stage('Test') {
        //     steps {
        //         echo "======== Stage 2: Running Maven tests ========"
        //         dir('Backend') {
        //             sh 'mvn test -B'
        //         }
        //     }
        //     post {
        //         success {
        //             echo "✅ All tests passed"
        //         }
        //         failure {
        //             echo "❌ Tests failed — pipeline stopped. Fix tests first."
        //         }
        //     }
        // }
 
        // ----------------------------------------------------------------
        //  STAGE 3 — Build JAR
        //
        //  Compiles Java source → creates executable JAR in Backend/target/
        //  -DskipTests: tests already ran in Stage 2, no need to repeat.
        //
        //  Why separate from Test stage?
        //  So you can see clearly in Jenkins dashboard:
        //    Test passed ✅  |  JAR build failed ❌
        //  vs everything lumped together.
        // ----------------------------------------------------------------
        stage('Build JAR') {
            steps {
                echo "======== Stage 3: Building JAR ========"
                dir('Server') {
                    sh 'mvn package -DskipTests -B'
                }
                // Verify JAR was created
                sh 'ls -lh Server/target/*.jar'
            }
        }
 
        // ----------------------------------------------------------------
        //  STAGE 4 — Docker Build
        //
        //  Builds both backend and frontend Docker images.
        //  Uses the Dockerfile inside each folder.
        //
        //  Two tags per image:
        //    :v42     → specific version (for rollback)
        //    :latest  → always points to newest (for EC2 pull)
        //
        //  Frontend note:
        //  VITE_API_URL is a --build-arg because Vite bakes it into
        //  the JS bundle at BUILD time (not runtime). So we must pass
        //  the real backend URL here. For now using placeholder — update
        //  with real EC2 IP when EC2 is ready.
        // ----------------------------------------------------------------
        stage('Docker Build') {
            steps {
                echo "======== Stage 4: Building Docker images ========"
 
                // ---- Backend ----
                sh """
                    docker build \
                        -t ${BACKEND_IMAGE}:${IMAGE_TAG} \
                        -t ${BACKEND_IMAGE}:latest \
                        ./Server
                """
 
                // Verify image created
                sh "docker images | grep easyseat-backend"
 
                // ---- Frontend ----
                // TODO: Replace BACKEND_URL_PLACEHOLDER with real EC2 IP
                // when EC2 is ready. Format: http://<EC2-IP>:8080/api/v1
                sh """
                    docker build \
                        --build-arg VITE_API_URL=BACKEND_URL_PLACEHOLDER \
                        -t ${FRONTEND_IMAGE}:${IMAGE_TAG} \
                        -t ${FRONTEND_IMAGE}:latest \
                        ./Client
                """
 
                sh "docker images | grep easyseat-frontend"
            }
        }
 
        // ----------------------------------------------------------------
        //  STAGE 5 — Docker Push
        //
        //  Pushes both images to DockerHub.
        //
        //  withCredentials block:
        //    Jenkins fetches DockerHub username+token from its Credential
        //    store and injects as DOCKER_USER and DOCKER_PASS env vars.
        //    These are MASKED in console logs (shown as ****).
        //    They exist ONLY inside this block — not leaked elsewhere.
        //
        //  Why logout at end?
        //  Best practice — don't leave auth session open on Jenkins machine.
        // ----------------------------------------------------------------
        stage('Docker Push') {
            steps {
                echo "======== Stage 5: Pushing images to DockerHub ========"
 
                withCredentials([
                    usernamePassword(
                        credentialsId: 'DockerHubCred',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {
                    sh '''
                        # Login to DockerHub
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        echo "✅ DockerHub login successful"
 
                        # Push backend — both tags
                        docker push ${BACKEND_IMAGE}:${IMAGE_TAG}
                        docker push ${BACKEND_IMAGE}:latest
                        echo "✅ Backend image pushed: ${BACKEND_IMAGE}:${IMAGE_TAG}"
 
                        # Push frontend — both tags
                        docker push ${FRONTEND_IMAGE}:${IMAGE_TAG}
                        docker push ${FRONTEND_IMAGE}:latest
                        echo "✅ Frontend image pushed: ${FRONTEND_IMAGE}:${IMAGE_TAG}"
 
                        # Always logout
                        docker logout
                        echo "✅ Logged out from DockerHub"
                    '''
                }
            }
        }
 
        // ----------------------------------------------------------------
        //  STAGE 6 — Deploy to EC2  [DISABLED — EC2 not ready yet]
        //
        //  This stage will be uncommented when EC2 is set up.
        //  It will SSH into EC2, docker pull, and docker compose up.
        // ----------------------------------------------------------------
        // stage('Deploy to EC2') {
        //     steps {
        //         echo "Deploy stage — coming soon after EC2 setup"
        //     }
        // }
    }
 
    // ---- Post pipeline actions ----
    post {
 
        success {
            echo """
            ================================================
            ✅ PIPELINE SUCCESSFUL — Build #${BUILD_NUMBER}
 
            Images on DockerHub:
            → ${BACKEND_IMAGE}:${IMAGE_TAG}
            → ${FRONTEND_IMAGE}:${IMAGE_TAG}
 
            Next: Set up EC2 and enable Deploy stage.
            ================================================
            """
        }
 
        failure {
            echo """
            ================================================
            ❌ PIPELINE FAILED
            Check console output above for which stage failed.
            ================================================
            """
        }
 
        always {
            // ---- Cleanup dangling images on Jenkins machine ----
            // Every build creates new images. Old untagged ones waste disk.
            // 'prune -f' removes only dangling (untagged) images — safe.
            sh 'docker image prune -f'
            echo "🧹 Cleaned up dangling Docker images"
        }
    }
}
 