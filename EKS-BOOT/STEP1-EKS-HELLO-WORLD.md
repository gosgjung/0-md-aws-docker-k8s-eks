

### 준비물

- iam 계정
  - 내 경우는 미리 만들어둔 iam 계정이 있다.
- 사용자 그룹, 정책들
  - 사용자 그룹에 iam 계정을 추가해둔 상태이고, 이 그룹에 정책들을 연결해둔 상태다.
  - EKS에 한정해서 추가해둔 정책들에 대해서는 별도의 문서에 모두 정리해둬야 겠다는 생각 중. ㅠㅠ
  - 사용자 그룹에 대한 개념도 정리해둘까 생각중이다.



### Spring Boot Application

스프링 부트 애플리케이션은 아래와 같은 구성으로 생성했다.

![](./img/STEP1-EKS-HELLO-WORLD/1.png)

<br>



그리고 Controller 를 아래와 같이 작성해줬다.

```java
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

    @GetMapping("/")
    public String index(){
        return "Hello World!";
    }
    
}
```

<br>



### jar 파일 빌드

```bash
$ gradlew bootJar
```

<br>



### Dockerfile 정의 및 이미지 빌드

도커파일은 아래와 같이 작성했다.

plain build 를 할 수도 있고, spring boot 의 특정 버전대부터는 컨테이너를 만들때 layertools를 통해 최적화되는 걸로 알고 있다.

하지만, 일단은 직접 최적화를 하는 것까지 포함한 Docker 이미지 빌드 스크립트를 추가했다.

```dockerfile
FROM openjdk:17-alpine AS jar-image
WORKDIR deploy
COPY build/libs/layering_docker_image279-v1.0.jar app.jar
RUN java -jar -Djarmode=layertools app.jar extract

FROM openjdk:17-alpine
WORKDIR deploy
COPY --from=jar-image deploy/dependencies/ ./
COPY --from=jar-image deploy/snapshot-dependencies/ ./
COPY --from=jar-image deploy/spring-boot-loader/ ./
COPY --from=jar-image deploy/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
```

- `COPY --from=jar-image deploy/dependencies/ ./`
  - jar 파일 내에서 deploy/dependencies 디렉터리 만을 복사해서 현재 WORKDIR 로 복사한다.
- `COPY --from=jar-image deploy/snapshot-dependencies/ ./`
  - jar 파일 내에서 deploy/snapshot-dependencies 디렉터리 만을 복사해서 현재 WORKDIR 로 복사한다.
- `COPY --from=jar-image deploy/spring-boot-loader/ ./`
  - jar 파일 내에서 deploy/spring-boot-loader 디렉터리 만을 복사해서 현재 WORKDIR 로 복사한다.
- `COPY --from=jar-image deploy/application/ ./` 
  - jar 파일 내에서 deploy/application 디렉터리 만을 복사해서 현재 WORKDIR 로 복사한다.

<br>



**이미지 빌드**<br>

```bash
$ docker build -t docker_k8s_app .
```

<br>



**컨테이너 실행**<br>

```bash
$ docker container run --rm -d -p 8080:8080 --name docker_k8s_app docker_k8s_app
```

<br>



동작 확인

```bash
$ curl http://localhost:8080
Hello World!
```





**이미지 확인**<br>

```bash
$ docker ps
```

<br>



**컨테이너 종료**

```bash
$ docker container stop docker_k8s_app
```

<br>



### IAM Role 추가한 것들

#### ECS IAM Role 추가

개발자 입장에서 추가한 거라 조금은 필요 없는 권한이 추가되있을 수도 있다. 개발도 빨리해야 하는데, 인프라도 알아야돼 이런건 물리적인 시간을 역행하는 시간여행자식 자본논리인듯 싶다.



> 참고자료 
>
> - [Amazon Elastic Container Registry 자격 증명기반 정책 예제 > Amazon ECR 콘솔 사용](https://docs.aws.amazon.com/ko_kr/AmazonECR/latest/userguide/security_iam_id-based-policy-examples.html)
>   - JSON 정책을 가져와서 사용했다.
>
> - [Pushing an image to ECR, getting "Retrying in ... seconds"](https://stackoverflow.com/questions/70828205/pushing-an-image-to-ecr-getting-retrying-in-seconds)
>   - 중간에 안될때가 있었는데, 어떤 권한 몇개가 없어서 그런거였다. (e.g. BatchGetImage 등등)
> - 제일 처음 봤던 자료
>   - [Amazon Elastic Container Registry 용 Identity and Access Management](https://docs.aws.amazon.com/ko_kr/AmazonECR/latest/userguide/security-iam.html)

특정 IAM 사용자의 IAM Role 이 필요한데, 내 경우는 사용자 그룹에 JSON 권한을 추가해줬다.

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "ecr:GetAuthorizationToken",
                "ecr:BatchCheckLayerAvailability",
                "ecr:GetDownloadUrlForLayer",
                "ecr:GetRepositoryPolicy",
                "ecr:DescribeRepositories",
                "ecr:ListImages",
                "ecr:DescribeImages",
                "ecr:BatchGetImage",
                "ecr:GetLifecyclePolicy",
                "ecr:GetLifecyclePolicyPreview",
                "ecr:ListTagsForResource",
                "ecr:DescribeImageScanFindings",
                "ecr:CreateRepository",
                "ecr:BatchGetImage",
                "ecr:BatchCheckLayerAvailability",
                "ecr:CompleteLayerUpload",
                "ecr:GetDownloadUrlForLayer",
                "ecr:InitiateLayerUpload",
                "ecr:PutImage",
                "ecr:UploadLayerPart"
            ],
            "Resource": "*"
        }
    ]
}
```



사용자 그룹에 인라인 정책을 연결해준다.

![](./img/STEP1-EKS-HELLO-WORLD/7.png)

<br>

![](./img/STEP1-EKS-HELLO-WORLD/8.png)

<br>



#### eksctl IAM Role 추가

> 참고자료
>
> - [AWS Identity and Access Management을 통한 액세스 제어](https://docs.aws.amazon.com/ko_kr/AWSCloudFormation/latest/UserGuide/using-iam-template.html)

<br>

IAM 사용자 그룹에 SQS, CloudFormation 관련 정책 추가해준다.

실습에서 사용하는 eks-sample 사용자는 eks-sample-user-group 이라는 사용자 그룹에 속하는데, 이 eks-sample-user-group 이라는 사용자 그룹에 SQS, CloudFormation 관련 정책들을 추가해줬다.

정책 JSON 은 아래와 같다.

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "sqs:*",
                "cloudformation:CreateStack",
                "cloudformation:DescribeStacks",
                "cloudformation:DescribeStackEvents",
                "cloudformation:DescribeStackResources",
                "cloudformation:GetTemplate",
                "cloudformation:ValidateTemplate"
            ],
            "Resource": "*"
        }
    ]
}
```



정책 추가 과정

![](./img/STEP1-EKS-HELLO-WORLD/9.png)



![](./img/STEP1-EKS-HELLO-WORLD/10.png)



![](./img/STEP1-EKS-HELLO-WORLD/11.png)



<br>





### ECR 리포지터리 생성

docker_k8s_app_repository

![](./img/STEP1-EKS-HELLO-WORLD/2.png)

<br>



![](./img/STEP1-EKS-HELLO-WORLD/3.png)

<br>



![](./img/STEP1-EKS-HELLO-WORLD/4.png)

<br>



![](./img/STEP1-EKS-HELLO-WORLD/5.png)

<br>



윈도우에서는 다른 명령어를 쓰라고 탭이 분리되어 있는데 윈도우 명령어가 안통해서 리눅스 명령어를 그대로 사용하니 되었다.

![](./img/STEP1-EKS-HELLO-WORLD/6.png)



각각의 명령어는 아래에 정리해뒀다.

```bash
$ aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin {계정id}.dkr.ecr.ap-northeast-2.amazonaws.com


$ docker build -t docker_k8s_app_repository .


$ docker tag docker_k8s_app_repository:latest {계정id}.dkr.ecr.ap-northeast-2.amazonaws.com/docker_k8s_app_repository:latest


$ docker push {계정id}.dkr.ecr.ap-northeast-2.amazonaws.com/docker_k8s_app_repository:latest
```







### eksctl 로 클러스터 생성

#### eksctl 로 클러스터 생성시, 비용 과금 유의필요

참고로 클러스터 생성에는 굉장히 오래 걸린다. 내가 직접 경험해본 바로는 20분 이상 걸렸었다. eksctl 사용시 주의해야 할 점 하나는 elastic ip 등 여러가지 부수적인 리소스들이 생성되는 것도 주의해야 한다. 리소스 삭제시에 모두 삭제해주자.

<br>

개인 실습용으로 한다고 해도 돈이 줄줄 새는거라 주의가 필요하다.

<br>

이런 이유로... 테라폼/테라포머로 EKS 리소스들을 추가/관리하는 것 관련해서 예제로 스터디 중인데, 별도의 문서에 정리 중이다. 테라폼/테라포머로 추가하면 리소스의 추가/삭제가 정해둔 것들만 추가하기에 별도의 비용은 내가 AWS에 요청한 리소스 사용량 만큼만 과금된다.

<br>



#### 클러스터 생성

내가 지정해준 클러스터 이름은 `eksctl-boot-study-1` 이다.

```bash
$ eksctl create cluster --name eksctl-boot-study-1 --region ap-northeast-2
```

<br>



#### 리소스 확인

```bash
$ kubec
```



