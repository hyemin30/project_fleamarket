# 중고거래 사이트 당근팔조

## 주요기능
판매게시글 등록, 게시글 조회, 댓글달기, 좋아요, 구매하기, 구매내역 보기

## 인프라 구조
![image](https://user-images.githubusercontent.com/116478121/220061504-a04864d1-e5e3-4e6f-b9a6-8e6050b70cc1.png)

## ERD
![스크린샷(11)](https://user-images.githubusercontent.com/95980876/213648476-9380cb80-ee59-40e7-a66c-c0c8f28025cb.png)

## API 설계
https://www.notion.so/b898a61a16594bf9af23a460289e4215?v=c40292364a6f42e3a690e3b60a95e197

## 트러블 슈팅
1. 게시글 전체 조회 할 때 Spring Data Jpa를 사용하였더니 댓글 Entity를 조회하는 쿼리가 전체 게시글 숫자만큼 실행됨<br>
  -> 해결 : Querydsl fetchjoin 기능을 사용하여 한번에 조회할 수 있도록 함

2. 게시글 등록 시 이미지를 S3에 업로드 하는데, 파일 이름이 같으면 덮어쓰기가 됨<br>
  -> 해결 : UUID를 사용하여 파일이름이 다 달라지게끔 

3. 로그인 성공 후에 jwt 토큰이 헤더에 들어가지 않음<br>
-> 해결 : RedirectAttributes를 이용하여 쿼리 파라미터로 전달

![1](https://user-images.githubusercontent.com/116478121/214867731-0273ffd2-bb3c-4cf7-934e-62c5c561c363.png)
![2](https://user-images.githubusercontent.com/116478121/214867738-8c597531-de9d-43a1-b237-4080c7a0c92f.png)
![3](https://user-images.githubusercontent.com/116478121/214867746-a2045217-ec3e-4156-b34a-c93fcfd00af5.png)
