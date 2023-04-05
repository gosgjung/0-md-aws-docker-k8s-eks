# Dockerfile 주요 명령어

매번 책 열어서 확인해보기에는 책 들고 다니기에도 열받고 해서 정리 시작 ㅋㅋ

핸드폰이든 컴퓨터든 브라우저로 언제든 열어서 확인해보기 위해 정리 시작!!

- [15단계로 배우는 도커와 쿠버네티스 - 140p Dockerfile 치트시트](http://www.yes24.com/Product/Goods/93317828)

<br>



참고자료

- [15단계로 배우는 도커와 쿠버네티스 - 140p Dockerfile 치트시트](http://www.yes24.com/Product/Goods/93317828)
- [공식문서 - Dockerfile reference](https://docs.docker.com/engine/reference/builder/)

<br>



FROM {이미지}{:태그}

- 컨테이너의 베이스 이미지를 지정



RUN {커맨드}, RUN ["커맨드", "파라미터1", "파라미터2"]

- FROM 의 베이스 이미지에서 커맨드를 실행



ADD {소스} {컨테이너 내의 경로}, ADD {"소스", ... "{컨테이너 내 경로}"}

- 소스(파일, 디렉터리, tar 파일, URL)를 컨테이너 내 경로에 복사



ENTRYPOINT ["실행가능한 파일", "파라미터1", "파라미터2"]

- ENRYPOINT 커맨드파라미터1, 파라미터2 (셸 행식)
- 컨테이너가 실행하는 파일을 설정



CMD ["실행 바이너리", "파라미터1", "파라미터 2"]

- CMD {커맨드} {셸 형식}
- CMD ["파라미터1", "파라미터2"] \(ENTRYPOINT 의 파라미터)
- 컨테이너 기동 시 실행될 커맨드를 지정



ENV {key} {value}

- ENV {key} = {value}
- 환경변수 설정



EXPOSE {PORT}

- EXPOSE {PORT} [{PORT} ... ] 
- 공개포트 설정



USER {유저명} | {UID}

- RUN, CMD, ENTRYPOINT 사용시 실행유저 지정



VOLUME {"/path"}

- 공유 가능한 볼륨을 마운트



WORKDIR /path

- RUN, CMD, ENTRYPOINT, COPY, ADD 의 작업 디렉터리 지정



ARG {이름}[={디폴트값}]

- 빌드할 때 넘길 인자를 정의
- `--build-arg {변수명}={값}`



LABEL {key}={value} {key}={value}

- 이미지의 메타데이터에 라벨을 추가



MAINTAINER {이름}

- 이미지의 메타데이터에 저작권을 추가









