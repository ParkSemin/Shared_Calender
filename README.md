# ---변경 사항 정리---  
* 날짜 : 2023-11-29
* 일시 : 21:55
* 비고 : 이 버전은 'semin_11_28'를 기준으로 수정되었음
  
1. FcmPush.kt 코드 추가 (fcm을 이용해서 앱을 깔고 앱 화면이 켜저있지 않은 기기들에게 알림이 감 = 백그라운드 상태일때만 알림 감)
2. PushDTO.kt 코드 추가 위의 내용을 저장
3. AddEventActivity.kt 에서 일정 변경내용을 fcm으로 보내는 것에 대한 코드 추가
4. 메인엑티비티.kt에 fcm 실시간데이터 베이스에 토큰 저장 코드 추가[
6. 일정 업데이트 아이콘은 런처 아이콘 사용, 일정 알람시간의 알림은 알람 아이콘으로 변경]
