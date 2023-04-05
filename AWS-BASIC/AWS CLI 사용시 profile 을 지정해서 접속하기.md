# AWS CLI 사용시 profile 을 지정해서 접속하기



### 참고자료

- [EKS 환경에서 kubeconfig 적용하기](https://halfmoon95.tistory.com/entry/EKS-%ED%99%98%EA%B2%BD%EC%97%90%EC%84%9C-kubeconfig-%EC%A0%81%EC%9A%A9%ED%95%98%EA%B8%B0)
- [EKS 환경에서 RBACK 적용하기](https://halfmoon95.tistory.com/entry/EKS-%ED%99%98%EA%B2%BD%EC%97%90%EC%84%9C-RBAC-%EC%A0%81%EC%9A%A9%ED%95%98%EA%B8%B0)



<br>



### aws 접속 profile 수정

`--profile` 옵션과 함께 profile 을 지정해서 명령어를 수행하면, 명령어에 지정한 profile 로 등록된 aws access key 를 참조해서 aws 명령어를 수행된다.

상용에서 AWS CLI 를 사용할 때 현재 접속된 프로필이 운영 profile 인지 파악하지 못하고 리소스를 삭제하는 등의 실수를 방지하려면 가급적 `--profile` 을 지정해서 명령어를 수행하는 것이 좋다. 

아래 예제의 credentials 파일에서의 `eks-sample` 이라는 프로필을 지정하기 위해 `--profile eks-sample` 과 같은 인자값을 aws 명령어에 전달해주면 된다.

<br>



**{사용자 홈디렉터리}/.aws/credentials 파일** 

```plain
[default]
aws_access_key_id = {ACCESS KEY}
aws_secret_access_key = {SECRET ACCESSS KEY}

[eks-sample]
aws_access_key_id = {ACCESS KEY}
aws_secret_access_key = {SECRET ACCESSS KEY}
```

<br>