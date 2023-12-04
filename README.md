# ---변경 사항 정리---  
* 날짜 : 2023-12-04
* 일시 : 01:55
* 비고 : 이 버전은 'doncici_12.4_end'를 기준으로 수정되었음
  
1. onChildChanged 리스너가 ScheduleData 객체의 어떠한 변경에도 updateAlarm 함수를 중복 호출 문제 확인
2. ScheduleData 클래스에 isDataChanged라는 새로운 메서드를 추가 -> 알림 설정에 중요한 필드(start_date, start_time, end_date, end_time, notificationTime)가 변경되었는지 확인
3. onChildChanged 리스너에서, 변경된 ScheduleData 객체와 기존 객체를 isDataChanged 메서드를 사용하여 비교 -> 중요한 필드에 실제 변경이 있었을 때만 updateAlarm을 호출
4. 로그 확인 결과 중복 호출 사라짐
5. 알림 설정에 30분전, 2시간전 추가