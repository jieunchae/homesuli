package com.ssafy.homesool.controller;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


import org.mariadb.jdbc.internal.logging.Logger;
import org.mariadb.jdbc.internal.logging.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import com.ssafy.homesool.dto.PhotoDto;
import com.ssafy.homesool.dto.RoomDto;
import com.ssafy.homesool.dto.UserDto;
import com.ssafy.homesool.entity.Room;
import com.ssafy.homesool.entity.Member;
import com.ssafy.homesool.service.PhotoService;
import com.ssafy.homesool.service.RoomService;

@RequiredArgsConstructor
@RestController
@RequestMapping("room")
@Api(tags = {"Room Controller"})
@Tag(name = "Room Controller", description = "미팅과 관련된 기능")
public class RoomController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final RoomService roomService;
	private final PhotoService photoService;

	@PostMapping
	@ApiOperation(value = "미팅 시작", notes = "새로 미팅을 주최한다. response로 10자리 code를 반환한다", response = RoomDto.InsertRoomInfo.class)
	@ApiResponses(value = {
		@ApiResponse(code = 201, message = "Created"),
		@ApiResponse(code = 400, message = "Bad Request"),
		@ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 403, message = "Forbidden"),
		@ApiResponse(code = 404, message = "Not Found")
	})
	private ResponseEntity<RoomDto.RoomResponse> addRoom(
		@ApiParam(value = "호스트 유저 id와 시작 시각", required = true) @RequestBody RoomDto.InsertRoomInfo insertRoomInfo) {
		logger.debug("add Room 호출\n" + insertRoomInfo.toString());
		//방 생성
		RoomDto.RoomResponse roomResponse = roomService.add(insertRoomInfo);
		//멤버 목록에 추가
		roomService.addMember(roomResponse.getCode(), insertRoomInfo.getHostId(), insertRoomInfo.getHostNickName(),1);
		return new ResponseEntity<>(roomResponse, HttpStatus.OK);
	}
	
	@PutMapping
	@ApiOperation(value = "미팅 종료", notes = "미팅을 종료하고 종료 시각을 기록한다.", response = RoomDto.UpdateRoomInfo.class)
	@ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 400, message = "Bad Request"),
		@ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 403, message = "Forbidden"),
		@ApiResponse(code = 404, message = "Not Found")
	})
	private ResponseEntity<Room> updateRoom(
		@ApiParam(value = "미팅 id와 종료 시각",required = true) @RequestBody RoomDto.UpdateRoomInfo updateRoomInfo){
		logger.debug(String.format("update Room {%s} end time {%s} 호출",updateRoomInfo.getRoomId(), updateRoomInfo.getEndTime().toString()));
		return new ResponseEntity<Room>(roomService.update(updateRoomInfo.getRoomId(), updateRoomInfo.getEndTime()),HttpStatus.OK);
	}
	
	@PostMapping("{code}/host")
	@ApiOperation(value = "미팅 시작 후 호스트 정보 업데이트", notes = "초기 정보 업데이트", response = String.class)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 400, message = "Bad Request"),
			@ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 403, message = "Forbidden"),
			@ApiResponse(code = 404, message = "Not Found")
	})
	private ResponseEntity<String> UpdateHost(
		@ApiParam(value = "방 코드",required = true, example = "A1B2C3D4E5") @PathVariable String code,
		@ApiParam(value = "호스트 유저 id와 시작 시각", required = true) @RequestBody RoomDto.InsertHostInfo insertHostInfo) {
		logger.debug("update host in Room 호출\n" );
		//방 제목 추가
		roomService.updateRoomName(insertHostInfo.getRoomId(), insertHostInfo.getRoomName());
		//호스트 닉네임 업데이트
		roomService.addMember(code, insertHostInfo.getHostId(), insertHostInfo.getHostNickName(), 1);
		return new ResponseEntity<>("update host", HttpStatus.OK);
	}
	
	@PostMapping("{code}/with/{userId}")
	@ApiOperation(value = "미팅 멤버 추가", notes = "미팅에 접속하고 멤버 목록에 추가한 후 response로 roomId를 보내준다.", response = Long.class)
	@ApiResponses(value = {
		@ApiResponse(code = 201, message = "Created"),
		@ApiResponse(code = 400, message = "Bad Request"),
		@ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 403, message = "Forbidden"),
		@ApiResponse(code = 404, message = "Not Found")
	})
	private ResponseEntity<Long> addMember(
		@ApiParam(value = "방 코드",required = true, example = "A1B2C3D4E5") @PathVariable String code,
		@ApiParam(value = "유저 id",required = true, example = "1404739104") @PathVariable long userId){
		logger.debug(String.format("add Member {%d} in {%s} 호출",userId,code));
		return new ResponseEntity<>(roomService.addMember(code,userId," ",2),HttpStatus.OK);
	}
	
	@PutMapping("{code}/with/{userId}")
	@ApiOperation(value = "멤버 닉네임 업데이트", notes = "멤버 닉네임 업데이트")
	@ApiResponses(value = {
		@ApiResponse(code = 201, message = "Created"),
		@ApiResponse(code = 400, message = "Bad Request"),
		@ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 403, message = "Forbidden"),
		@ApiResponse(code = 404, message = "Not Found")
	})
	private ResponseEntity<Long> UpdateMember(
		@ApiParam(value = "방 코드",required = true, example = "A1B2C3D4E5") @PathVariable String code,
		@ApiParam(value = "유저 id",required = true, example = "1404739104") @PathVariable long userId,
		@ApiParam(value = "유저 닉네임",required = true, example = "지은짱") @RequestBody RoomDto.UpdateMemberInfo updateMemberInfo){
		logger.debug(String.format("add Member {%d} in {%s} 호출",userId,code));
		return new ResponseEntity<>(roomService.addMember(code,userId,updateMemberInfo.getNickName(),2),HttpStatus.OK);
	}
	
	@PostMapping("photo")
	@ApiOperation(value = "사진 업로드", notes = "서버에 스크린샷을 저장한다.", response = String.class)
	@ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 400, message = "Bad Request"),
		@ApiResponse(code = 401, message = "Unauthorized"),
		@ApiResponse(code = 403, message = "Forbidden"),
		@ApiResponse(code = 404, message = "Not Found")
	})
	private ResponseEntity<String> UploadFile(
			@ApiParam(value = "업로드할 사진 정보와 해당 미팅의 id") @RequestBody PhotoDto.PhotoRequest photoRequest
			) {
			logger.debug("사진 업로드 시작\n");
			/*
			String filename = "";
			String access_path="";
			try {
				// 업로드되는 파일 이름 중간에 날짜줄거임
				SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
				Date nowdate = new Date();
				String dateString = formatter.format(nowdate);

				// 웹서비스 경로 지정
				// String root_path =
				// request.getSession().getServletContext().getRealPath("\\");

				String root_path = "C:\\Users\\multicampus\\git\\test\\";
				String real_path = "/home/ubuntu/picture/";
				// String attach_path = "resources\\upload\\";
				filename = dateString + "_" + uploadfile.getOriginalFilename();

				FileOutputStream fos = new FileOutputStream(real_path + filename);
				// 파일 저장할 경로 + 파일명을 파라미터로 넣고 fileOutputStream 객체 생성하고
				InputStream is = uploadfile.getInputStream();
				// file로 부터 inputStream을 가져온다.

				int readCount = 0;
				byte[] buffer = new byte[2048];
				// 파일을 읽을 크기 만큼의 buffer를 생성하고
				// ( 보통 1024, 2048, 4096, 8192 와 같이 배수 형식으로 버퍼의 크기를 잡는 것이 일반적이다.)

				while ((readCount = is.read(buffer)) != -1) {
					// 파일에서 가져온 fileInputStream을 설정한 크기 (1024byte) 만큼 읽고
					fos.write(buffer, 0, readCount);
					// 위에서 생성한 fileOutputStream 객체에 출력하기를 반복한다
				}

				access_path = "http:/k3a503.p.ssafy.io/images/";

			} catch (Exception ex) {
				throw new RuntimeException("file Save Error");
			}
			*/
			try {
				photoService.add(photoRequest.getRoomId(),photoRequest.getImg());
			} catch (Exception ex) {
				return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			return new ResponseEntity<>(photoRequest.getImg(), HttpStatus.OK);
	}
}
