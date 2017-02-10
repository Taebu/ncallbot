package kr.co.cashq.ncall_bot;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;
import static java.lang.Math.toIntExact;


//import com.nostech.safen.SafeNo;

/**
 * prq_cdr 테이블 관련 객체
 * 2017-02-06 (월) 오전 10:42
 * @author Taebu
 *  
 */
public class Ncall_cmd_queue {
	
	/**
	 * prq_cdr 테이블의 데이터를 처리하기 위한 주요한 처리를 수행한다.
	 */
	public static void doMainProcess() {
		Connection con = DBConn.getConnection();
		
		String ns_tel	="";				
		String naver_result="";
		String ns_result="";
		String[] ncall_info = new String[5];
		int[] ncall_report = new int[6];
		
		int ns_success=0;
		int ns_fail=0;
		int ns_total=0;
		long nr_start=0L; 
		if (con != null) {
			MyDataObject dao = new MyDataObject();
			StringBuilder sb = new StringBuilder();

			sb.append("select * from ncall_sync  ");
			//sb.append("WHERE ns_status=0 ");
			//sb.append("limit 1 ;");

			try {
				APISearchLocal asl = new APISearchLocal();			
				dao.openPstmt(sb.toString());
				dao.setRs(dao.pstmt().executeQuery());
				/*****************************************************
				* 2-2. 리스트 출력 
				* SELECT TIMESTAMPDIFF(DAY,'2009-05-18','2009-07-29');
				***************************************************/
				nr_start=System.currentTimeMillis() / 1000;
				while(dao.rs().next()) 
				{
					
					ns_tel=chkValue(dao.rs().getString("ns_tel"));
					naver_result=asl.search_naver(ns_tel);
					//Utils.getLogger().info("message : "+naver_result);
					naver_result=asl.get_json(naver_result);

					if(!naver_result.equals("0")){
						System.out.println("일치");
						ns_result="1";
						ns_success++;
					}else{
						System.out.println("불일치");
						ns_result="2";
						ns_fail++;
					}					
					NCALL_LOG.heart_beat = 1;
					
					//String hist_table = DBConn.isExistTableYYYYMM();
					ncall_info[0]=ns_result;     
					ncall_info[1]=dao.rs().getString("ns_stno");
					/* ncall_sync를 갱신합니다. */
					set_ncall(ncall_info);
					/* ncall_log_Ym 을 입력합니다. */
					//set_ncall(ncall_info);
				}/* while(dao.rs().next()){...} */
				/* set_report 을 입력합니다.
				 * 		 
				 * nr_stunix int unsigned not null default 0 comment "시작 유닉스타임",
				 * nr_edunix int unsigned not null default 0 comment "종료 유닉스타임",
				 * nr_success int unsigned not null default 0 comment "성공 갯수", 
				 * nr_fail int unsigned not null default 0 comment "실패 갯수", 
				 * nr_total int unsigned not null default 0 comment "전체 갯수", 
				 * nr_datetime datetime not null default '1970-01-01 00:00:00' comment "로그기록일"
				 * 
				 * 		dao.pstmt().setInt(1, str[0]);     //nr_stunix
						dao.pstmt().setLong(2, nr_end); //nr_edunix
						dao.pstmt().setInt(3, str[1]);     //nr_success
						dao.pstmt().setInt(4, str[2]);        //nr_fail
						dao.pstmt().setInt(5, str[3]);        //nr_total
				 *  */
				ns_total=ns_success+ns_fail;
				ncall_report[0]=toIntExact(nr_start);
				ncall_report[1]=ns_success;
				ncall_report[2]=ns_fail;
				ncall_report[3]=ns_total;
				
				set_report(ncall_report);				
			} catch (SQLException e) {
				Utils.getLogger().warning(e.getMessage());
				DBConn.latest_warning = "ErrPOS001";
				e.printStackTrace();
			}
			catch (Exception e) {
				Utils.getLogger().warning(e.getMessage());
				DBConn.latest_warning = "ErrPOS002";
				Utils.getLogger().warning(Utils.stack(e));
			}
			finally {
				dao.closePstmt();
			}
			
		}
	}

	

    /**
     * chkValue
	 *  데이터 유효성 null 체크에 대한 값을 "" 로 리턴한다.
     * @param str
     * @return String
     */
	public static String chkValue(String str)
	{
		String retVal="";

		try{
				retVal=str==null?"":str;
		}catch(NullPointerException e){
			
		}
		return retVal;
	}
	
	/**
	 * 금일 보낸 발송 갯수 갱신
	 *
	 * @author Taebu Moon <mtaebu@gmail.com>
	 * @param string $table 게시판 테이블
	 * @param string $id 게시물번호
	 * @return array
	 */
	private static void set_ncall(String[] str)
    {
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		
		sb.append("UPDATE ncall_sync SET ");
		sb.append("ns_status=?, ");
		sb.append("ns_datetime=now() ");
		sb.append(" WHERE ns_stno=? ");
			
		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, str[0]);
			dao.pstmt().setString(2, str[1]);
			/* 조회한 콜로그의 일 발송량 갱신 */
			dao.pstmt().executeUpdate();

						
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS011";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS012";
		}
		finally {
			dao.closePstmt();
		}
    }
	

	/**
	 * 
	 * @param toint
	 */
	private static void set_report(int[] toint)
    {
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		long nr_end=System.currentTimeMillis() / 1000;
		/*
		 * nr_stunix int unsigned not null default 0 comment "시작 유닉스타임",
			nr_edunix int unsigned not null default 0 comment "종료 유닉스타임",
			nr_success int unsigned not null default 0 comment "성공 갯수", 
			nr_fail int unsigned not null default 0 comment "실패 갯수", 
			nr_total int unsigned not null default 0 comment "전체 갯수", 
			nr_datetime datetime not null default '1970-01-01 00:00:00' comment "로그기록일"
		 * */
		sb.append("INSERT INTO ncall_report SET ");
		sb.append("nr_stunix=?, ");
		sb.append("nr_edunix=?, ");
		sb.append("nr_success=?, ");
		sb.append("nr_fail=?, ");
		sb.append("nr_total=?, ");
		sb.append("nr_datetime=now() ");

		try {
			dao.openPstmt(sb.toString());
			System.out.println(sb.toString());
			dao.pstmt().setInt(1, toint[0]);     //nr_stunix
			dao.pstmt().setLong(2, nr_end); //nr_edunix
			dao.pstmt().setInt(3, toint[1]);     //nr_success
			dao.pstmt().setInt(4, toint[2]);        //nr_fail
			dao.pstmt().setInt(5, toint[3]);        //nr_total
			
			/* 조회한 콜로그의 일 발송량 갱신 */
			dao.pstmt().executeUpdate();
					
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS011";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS012";
		}
		finally {
			dao.closePstmt();
		}
    }
	// yyyy-MM-dd HH:mm:ss.0 을 yyyy-MM-dd HH:mm:ss날짜로 변경
	public static String chgDatetime(String str)
	{
		String retVal="";

		try{
		SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date historyDate = simpleDate.parse(str);
		retVal=simpleDate.format(historyDate);
		}catch(ParseException e){
		}
		return retVal;
	}

	  /**
	  * 정규식 패턴 검증
	  * @param pattern
	  * @param str
	  * @return
	  */
	
	 public static boolean checkPattern(String pattern, String str){
	  boolean okPattern = false;
	  String regex = null;
	  
	  pattern = pattern.trim();
	  
	  //숫자 체크
	  if("num".equals(pattern)){
	   regex = "^[0-9]*$";
	  }
	  
	  //영문 체크
	  
	  //이메일 체크
	  if("email".equals(pattern)){
	   regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
	  }
	  
	  //전화번호 체크
	  if("tel".equals(pattern)){
	   regex = "^\\d{2,3}-\\d{3,4}-\\d{4}$";
	  }
	  
	  //휴대폰번호 체크
	  if("phone".equals(pattern)){
	   regex = "^01[016789]-?(\\d{3}|\\d{4})-?\\d{4}$";
	  }
	  //System.out.println(regex);
	  okPattern = Pattern.matches(regex, str);
	  return okPattern;
	 }
}
