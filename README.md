# ---변경 사항 정리---  
* 날짜 : 2023-11-27
* 일시 : 03:13
  
1. 메인엑티비티가 열릴때마다 데이터베이스에 있는 일정 시작정보와 몇분전 값을 가져와 그에맞게 알림설정 
2. AlarmReceiver.kt (알람 내용관리)추가
3. activity_noticication_settings.xml (알람설정 화면) 추가
4. Notification~~Activity.kt (알람설정 화면 코드) 추가
5. 알람 설정 후 에드이벤트 화면 같을때 설정하던 데이터 유지
6. 과거 시간대의 알림은 설정되지 않음
7. scheduleData.kt 에 변수 notificationTime(몇분전값) 추가