# STEP2

이름을 뭘로 지을지 난감..

<br>



### 참고
- [OIDC 란?](https://hudi.blog/open-id/) 
- [Installing the AWS Load Balancer Controller add-on](https://docs.aws.amazon.com/eks/latest/userguide/aws-load-balancer-controller.html)
- [jetstack github](https://github.com/jetstack/)
- [cert-manager releases, github](https://github.com/cert-manager/cert-manager/releases/tag/v1.11.1)
- [github.com/kubernetes-sigs/aws-load-balancer-controller](https://github.com/kubernetes-sigs/aws-load-balancer-controller)
  - full.yaml, ingclass.yaml

- https://github.com/kubernetes-sigs/aws-load-balancer-controller/blob/main/docs/examples/2048/2048_full.yaml

- https://docs.aws.amazon.com/eks/latest/userguide/alb-ingress.html
- https://docs.aws.amazon.com/eks/latest/userguide/aws-load-balancer-controller.html
- https://kubernetes.io/docs/concepts/services-networking/ingress/

<br>





### ekscluster 의 oidc 권한 승인

윈도우환경에서는 git bash 를 사용해야 명령어를 실행가능하다.

```plain
$ oidc_id=$(aws eks describe-cluster --name eksctl-boot-study-1 --query "cluster.identity.oidc.issuer" --output text | cut -d '/' -f 5)

$ echo $oidc_id
{open id}

$ aws iam list-open-id-connect-providers | grep $oidc_id

```



IAM Open Id 접속 제공자 (IAM Open Id Cnnect Provider)를 `eksctl-boot-study-1` 에 생성한다.

```bash
$ eksctl utils associate-iam-oidc-provider --cluster eksctl-boot-study-1 --approve

2023-04-17 18:48:26 [ℹ]  will create IAM Open ID Connect provider for cluster "eksctl-boot-study-1" in "ap-northeast-2"
2023-04-17 18:48:26 [✔]  created IAM Open ID Connect provider for cluster "eksctl-boot-study-1" in "ap-northeast-2"
```

<br>



### load balancer controller 에 대한 iam_policy 생성 작업

먼저 aws 에서 공식으로 제공해주는 aws-load-balancer-controller 에 대한 JSON 기반의 iam_policy 명세서를 다운로드하자.

```bash
$ curl -o iam_policy.json https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.4.4/docs/install/iam_policy.json
```

<br>



이제 다운로드 받은 iam_policy.json 파일을 이용해서 정책을 생성하는데, 생성하는 정책의 정책 명은 `AWSLoadBalancerControllerIAMPolicy` 다.

```bash
$ aws iam create-policy \
	--policy-name AWSLoadBalancerControllerIAMPolicy \
	--policy-document file://iam_policy.json

... 

{
    "Policy": {
        "PolicyName": "AWSLoadBalancerControllerIAMPolicy",
        "PolicyId": "{정책 ID}",
        "Arn": "arn:aws:iam::{IAM 계정번호}:policy/AWSLoadBalancerControllerIAMPolicy",
        "Path": "/",
        "DefaultVersionId": "v1",
        "AttachmentCount": 0,
        "PermissionsBoundaryUsageCount": 0,
        "IsAttachable": true,
        "CreateDate": "2023-04-17T09:52:48+00:00",
        "UpdateDate": "2023-04-17T09:52:48+00:00"
    }
}
```

<br>



이번에는 STEP1 에서 생성했던 eks 클러스터인 `eksctl-boot-study-1` 에 대해 방금 생성한 AWSLoadBalancerControllerIAMPolicy 이 적용된 iamservice account 를 'aws-load-balancer-controller' 라는 이름으로 생성한다.

```bash
$ eksctl create iamserviceaccount \
	--cluster=eksctl-boot-study-1 \
	--namespace=kube-system \
	--name=aws-load-balancer-controller \
	--role-name "AmazonEKSLoadBalancerControllerRole" \
	--attach-policy-arn=arn:aws:iam::693608546603:policy/AWSLoadBalancerControllerIAMPolicy 
	--approve
	
...

2023-04-17 19:00:29 [ℹ]  1 iamserviceaccount (kube-system/aws-load-balancer-controller) was included (based on the include/exclude rules)
... 
```

<br>



### 인증서 적용

```bash
$ kubectl apply \
	--validate=false 
	-f https://github.com/cert-manager/cert-manager/releases/download/v1.11.1/cert-manager.yaml



namespace/cert-manager created
customresourcedefinition.apiextensions.k8s.io/clusterissuers.cert-manager.io created
customresourcedefinition.apiextensions.k8s.io/challenges.acme.cert-manager.io created
customresourcedefinition.apiextensions.k8s.io/certificaterequests.cert-manager.io created
customresourcedefinition.apiextensions.k8s.io/issuers.cert-manager.io created
customresourcedefinition.apiextensions.k8s.io/certificates.cert-manager.io created
customresourcedefinition.apiextensions.k8s.io/orders.acme.cert-manager.io created
serviceaccount/cert-manager-cainjector created
serviceaccount/cert-manager created
serviceaccount/cert-manager-webhook created
configmap/cert-manager-webhook created
clusterrole.rbac.authorization.k8s.io/cert-manager-cainjector created
clusterrole.rbac.authorization.k8s.io/cert-manager-controller-issuers created
clusterrole.rbac.authorization.k8s.io/cert-manager-controller-clusterissuers created
clusterrole.rbac.authorization.k8s.io/cert-manager-controller-certificates created
clusterrole.rbac.authorization.k8s.io/cert-manager-controller-orders created
clusterrole.rbac.authorization.k8s.io/cert-manager-controller-challenges created
clusterrole.rbac.authorization.k8s.io/cert-manager-controller-ingress-shim created
clusterrole.rbac.authorization.k8s.io/cert-manager-view created
clusterrole.rbac.authorization.k8s.io/cert-manager-edit created
clusterrole.rbac.authorization.k8s.io/cert-manager-controller-approve:cert-manager-io created
clusterrole.rbac.authorization.k8s.io/cert-manager-controller-certificatesigningrequests created
clusterrole.rbac.authorization.k8s.io/cert-manager-webhook:subjectaccessreviews created
clusterrolebinding.rbac.authorization.k8s.io/cert-manager-cainjector created
clusterrolebinding.rbac.authorization.k8s.io/cert-manager-controller-issuers created
clusterrolebinding.rbac.authorization.k8s.io/cert-manager-controller-clusterissuers created
clusterrolebinding.rbac.authorization.k8s.io/cert-manager-controller-certificates created
clusterrolebinding.rbac.authorization.k8s.io/cert-manager-controller-orders created
clusterrolebinding.rbac.authorization.k8s.io/cert-manager-controller-challenges created
clusterrolebinding.rbac.authorization.k8s.io/cert-manager-controller-ingress-shim created
clusterrolebinding.rbac.authorization.k8s.io/cert-manager-controller-approve:cert-manager-io created
clusterrolebinding.rbac.authorization.k8s.io/cert-manager-controller-certificatesigningrequests created
clusterrolebinding.rbac.authorization.k8s.io/cert-manager-webhook:subjectaccessreviews created
role.rbac.authorization.k8s.io/cert-manager-cainjector:leaderelection created
role.rbac.authorization.k8s.io/cert-manager:leaderelection created
role.rbac.authorization.k8s.io/cert-manager-webhook:dynamic-serving created
rolebinding.rbac.authorization.k8s.io/cert-manager-cainjector:leaderelection created
rolebinding.rbac.authorization.k8s.io/cert-manager:leaderelection created
rolebinding.rbac.authorization.k8s.io/cert-manager-webhook:dynamic-serving created
service/cert-manager created
service/cert-manager-webhook created
deployment.apps/cert-manager-cainjector created
deployment.apps/cert-manager created
deployment.apps/cert-manager-webhook created
mutatingwebhookconfiguration.admissionregistration.k8s.io/cert-manager-webhook created
validatingwebhookconfiguration.admissionregistration.k8s.io/cert-manager-webhook created

```





### kubernetes-sigs 공식 github 에서 ingress 및 기타 리소스 매니페스트 파일 다운로드 및 클러스터에 배포

```bash
$ curl -Lo v2_5_0_full.yaml https://github.com/kubernetes-sigs/aws-load-balancer-controller/releases/download/v2.5.0/v2_5_0_full.yaml

...
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
  0     0    0     0    0     0      0      0 --:--:-- --:--:-- --:--:--     0
100 36558  100 36558    0     0  30080      0  0:00:01  0:00:01 --:--:--  512k



$ sed -i.bak -e '480,488d' ./v2_5_0_full.yaml



# kubernetes 제공 full.yaml 내의 your-cluster-name 을 모두 eksctl-boot-study-1 로 치환
$ sed -i.bak -e 's|your-cluster-name|eksctl-boot-study-1|' ./v2_5_0_full.yaml


# 수정한 yaml 파일을 클러스터에 적용
$ kubectl apply -f v2_5_0_full.yaml


customresourcedefinition.apiextensions.k8s.io/ingressclassparams.elbv2.k8s.aws created
serviceaccount/aws-load-balancer-controller created
role.rbac.authorization.k8s.io/aws-load-balancer-controller-leader-election-role created
clusterrole.rbac.authorization.k8s.io/aws-load-balancer-controller-role created
rolebinding.rbac.authorization.k8s.io/aws-load-balancer-controller-leader-election-rolebinding created
clusterrolebinding.rbac.authorization.k8s.io/aws-load-balancer-controller-rolebinding created
service/aws-load-balancer-webhook-service created
deployment.apps/aws-load-balancer-controller created
certificate.cert-manager.io/aws-load-balancer-serving-cert created
issuer.cert-manager.io/aws-load-balancer-selfsigned-issuer created
mutatingwebhookconfiguration.admissionregistration.k8s.io/aws-load-balancer-webhook created
validatingwebhookconfiguration.admissionregistration.k8s.io/aws-load-balancer-webhook created

...
```





이번에는 ingress 를 다운로드 받아서 적용하자.

```bash
# download
$ curl -Lo v2_5_0_ingclass.yaml https://github.com/kubernetes-sigs/aws-load-balancer-controller/releases/download/v2.5.0/v2_5_0_ingclass.yaml

... 
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
  0     0    0     0    0     0      0      0 --:--:-- --:--:-- --:--:--     0
100   418  100   418    0     0    369      0  0:00:01  0:00:01 --:--:--   641


$ kubectl apply -f v2_5_0_ingclass.yaml
ingressclass.networking.k8s.io/alb created



```

<br>



이제 load-balance-controller 가 잘 설치됐는지 확인해본다.

```bash
$ kubectl get deployment -n kube-system aws-load-balancer-controller
NAME                           READY   UP-TO-DATE   AVAILABLE   AGE
aws-load-balancer-controller   0/1     1            0           4m
```





### ingress 배포

https://github.com/kubernetes-sigs/aws-load-balancer-controller/blob/main/docs/examples/2048/2048_full.yaml 의 제일 하단의 ingress 를 정의한 부분이 있는데 그곳의 내용을 name 만 바꿔서 아래와 같이 적용



```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  namespace: default
  name: eksctl-boot-study-1
  annotations:
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/target-type: ip
spec:
  ingressClassName: alb
  rules:
    - http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: eksctl-boot-study-1
                port:
                  number: 80
```

























