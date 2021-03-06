package com.crossit.controller;

import com.crossit.annotation.CurrentUser;
import com.crossit.constant.Method;
import com.crossit.domain.BoardDTO;
import com.crossit.domain.FileDTO;
import com.crossit.entity.Member;
import com.crossit.repository.MemberRepository;
import com.crossit.service.BoardService;
import com.crossit.util.UiUtils;
import com.crossit.view.BoardListViewByJob;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class BoardController extends UiUtils {

	@Autowired
	private BoardService boardService;

	private final MemberRepository memberRepository;



	@GetMapping("/board/write")
	public String openBoardWrite(@ModelAttribute("params") BoardDTO params,
								 @RequestParam(value = "id", required = false) Long id,
								 Model model,
								 Principal principal,
								 @CurrentUser Member member) {
		model.addAttribute("current", member);
		if (id == null) {
			BoardDTO boardDTO = new BoardDTO();
			boardDTO.setWriter(principal.getName());
			model.addAttribute("board", boardDTO);
		} else {
			BoardDTO board = boardService.getBoardDetail(id);
			if (board == null || "Y".equals(board.getDeleteYn())) {
				return showMessageWithRedirect("없는 게시글이거나 이미 삭제된 게시글입니다.", "admin/mylog", Method.GET, null, model);
			}
			model.addAttribute("board", board);

			List<FileDTO> fileList = boardService.getAttachFileList(id);
			model.addAttribute("fileList", fileList);
		}

		return "board/write";
	}

	@PostMapping(value = "/board/register")
	public String registerBoard(final BoardDTO params, final MultipartFile[] files, Model model) {
		Map<String, Object> pagingParams = getPagingParams(params);
		try {
			boolean isRegistered = boardService.registerBoard(params, files);
			if (isRegistered == false) {
				return showMessageWithRedirect("게시글 등록에 실패하였습니다.", "/board/list", Method.GET, pagingParams, model);
			}
		} catch (DataAccessException e) {
			return showMessageWithRedirect("데이터베이스 처리 과정에 문제가 발생하였습니다.", "/board/list", Method.GET, pagingParams, model);

		} catch (Exception e) {
			return showMessageWithRedirect("시스템에 문제가 발생하였습니다.", "/board/list", Method.GET, pagingParams, model);
		}

		return showMessageWithRedirect("게시글 등록이 완료되었습니다.", "admin/mylog", Method.GET, pagingParams, model);
	}


	@GetMapping("/board/list")
	public String openBoardList(@ModelAttribute("params") BoardDTO params, Model model) {
		List<BoardDTO> boardList = boardService.getBoardList(params);
		model.addAttribute("boardList", boardList);

		return "board/list";
	}

	@GetMapping(value = "/board/view")
	public String openBoardDetail(@ModelAttribute("params") BoardDTO params, @RequestParam(value = "id", required = false) Long id, Model model, @CurrentUser Member member) {

		if (id == null) {
			return showMessageWithRedirect("올바르지 않은 접근입니다.", "admin/mylog", Method.GET, null, model);
		}

		model.addAttribute("current", member);


		BoardDTO board = boardService.getBoardDetail(id);

		Member writer = memberRepository.findByNickname(board.getWriter());

		model.addAttribute("writer", writer);


		System.out.println(board.getCategory());
		if (board == null || "Y".equals(board.getDeleteYn())) {
			return showMessageWithRedirect("없는 게시글이거나 이미 삭제된 게시글입니다.", "admin/mylog", Method.GET, null, model);
		}
		model.addAttribute("board", board);

		List<FileDTO> fileList = boardService.getAttachFileList(id); // 추가된 로직
		model.addAttribute("fileList", fileList); // 추가된 로직

		return "board/view";
	}

	@PostMapping("/board/delete")
	public String deleteBoard(@ModelAttribute("params") BoardDTO params, @RequestParam(value = "id", required = false) Long id, Model model) {
		if (id == null) {
			return showMessageWithRedirect("올바르지 않은 접근입니다.", "/board/list", Method.GET, null, model);
		}

		Map<String, Object> pagingParams = getPagingParams(params);
		try {
			boolean isDeleted = boardService.deleteBoard(id);
			if (isDeleted == false) {
				return showMessageWithRedirect("게시글 삭제에 실패하였습니다.", "admin/mylog", Method.GET, pagingParams, model);
			}
		} catch (DataAccessException e) {
			return showMessageWithRedirect("데이터베이스 처리 과정에 문제가 발생하였습니다.", "/board/list", Method.GET, pagingParams, model);

		} catch (Exception e) {
			return showMessageWithRedirect("시스템에 문제가 발생하였습니다.", "/board/list", Method.GET, pagingParams, model);
		}

		return showMessageWithRedirect("게시글 삭제가 완료되었습니다.", "admin/mylog", Method.GET, pagingParams, model);
	}


	@GetMapping("/board/category")
	@ResponseBody
	public List<BoardDTO> getBoardList() {
		List<BoardDTO> boardList = boardService.getList();
		return boardList;
	}


	@GetMapping("/board/job")
	@ResponseBody
	public List<BoardListViewByJob> getBoardListViewByJob() {
		List<BoardListViewByJob> boardList = boardService.getListByJob();
		return boardList;
	}

	@GetMapping("/board/{nickname}")
	@ResponseBody
	public List<BoardDTO> getBoardListByNickname(@PathVariable String nickname) {
		List<BoardDTO> boardList = boardService.getBoardListByNickname(nickname);
		return boardList;
	}

}
