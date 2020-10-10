package com.lfc.phicomm.dc1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;

@RestController
public class Dc1Ctrl {
	
	private static Logger log = LoggerFactory.getLogger(Dc1Ctrl.class);
	
	private String statusStr = "";
	
	@GetMapping("/index")
	public ModelAndView getIndex(Model model) {
		if(!NettyServer.requestFlag) {
			model.addAttribute("disable", "���3������");
			return new ModelAndView("index");
		}else {
			model.addAttribute("disable", "");
		}
		JSONObject paramObj = new JSONObject();
		String uuid = "T" + System.currentTimeMillis();
		NettyServer.sendStrToClient(createJSON("datapoint", paramObj, uuid));
		sleep(200);
		String receiveStr = NettyServer.getReceiveString();
		if(StringUtils.isEmpty(receiveStr)) {
			log.error("���յ����ַ�");
			setModel(model, "", "", "");
			return new ModelAndView("index");
		}
		JSONObject receiveObj = JSONObject.parseObject(receiveStr);
		if(!uuid.equals(receiveObj.getString("uuid"))) {
			log.error("���յ���uuid��һ��");
			setModel(model, "", "", "");
			return new ModelAndView("index");
		}
		if(!"200".equals(receiveObj.getString("status"))) {
			log.error(receiveObj.getString("msg"));
			setModel(model, "", "", "");
			return new ModelAndView("index");
		}
		JSONObject resultObj = receiveObj.getJSONObject("result");
		if(!resultObj.isEmpty()) {
			setModel(model, resultObj.getString("I"), resultObj.getString("V"), resultObj.getString("P"));
			setSwitchModel(model, resultObj.getString("status"));
		}
		return new ModelAndView("index");
	}
	
	@GetMapping("/set/{switch}")
	public String setSwitch(@PathVariable("switch") String paramStr, Model model) {
		JSONObject resutlJson = new JSONObject();
		resutlJson.put("flag", "1");
		if(!NettyServer.requestFlag) {
			log.error("���3������");
			resutlJson.put("msg", "���3������");
			return resutlJson.toJSONString();
		}
		if(StringUtils.isEmpty(statusStr)) {
			log.error("��ѯ���Ĳ���״̬�����޷�����״̬");
			resutlJson.put("msg", "��ѯ���Ĳ���״̬�����޷�����״̬");
			return resutlJson.toJSONString();
		}
		String switchStr = statusStr;
		switch(paramStr) {
		case "main0":
			switchStr = statusStr.substring(0, 3) + "0";
			break;
		case "main1":
			switchStr = statusStr.substring(0, 3) + "1";
			break;
		case "switch10":
			switchStr = statusStr.substring(0, 2) + "0" + statusStr.substring(3);
			break;
		case "switch11":
			switchStr = statusStr.substring(0, 2) + "1" + statusStr.substring(3);
			break;
		case "switch20":
			switchStr = statusStr.substring(0, 1) + "0" + statusStr.substring(2);
			break;
		case "switch21":
			switchStr = statusStr.substring(0, 1) + "1" + statusStr.substring(2);
			break;
		case "switch30":
			switchStr = "0" + statusStr.substring(1);
			break;
		case "switch31":
			switchStr = "1" + statusStr.substring(1);
			break;
		default :
			log.error("�����������");
			resutlJson.put("msg", "�����������");
			return resutlJson.toJSONString();
		}
		log.info("���ÿ���״̬Ϊ��{}", switchStr);
		JSONObject paramObj = new JSONObject();
		int switchInt = Integer.parseInt(switchStr);
		paramObj.put("status", switchInt);
		String uuid = "T" + System.currentTimeMillis();
		NettyServer.sendStrToClient(createJSON("datapoint=", paramObj, uuid));
		sleep(200);
		String receiveStr = NettyServer.getReceiveString();
		if(StringUtils.isEmpty(receiveStr)) {
			log.error("���յ����ַ�");
			resutlJson.put("msg", "���յ����ַ�");
			return resutlJson.toJSONString();
		}
		JSONObject receiveObj = JSONObject.parseObject(receiveStr);
		if(!uuid.equals(receiveObj.getString("uuid"))) {
			log.error("���յ���uuid��һ��");
			resutlJson.put("msg", "���յ���uuid��һ��");
			return resutlJson.toJSONString();
		}
		if(!"200".equals(receiveObj.getString("status"))) {
			log.error(receiveObj.getString("msg"));
			return resutlJson.toJSONString();
		}
		JSONObject resultObj = receiveObj.getJSONObject("result");
		if(!resultObj.isEmpty()) {
			setSwitchModel(model, resultObj.getString("status"));
		}
		resutlJson.put("flag", "0"); 
		resutlJson.put("msg", "");
		return resutlJson.toJSONString();
	}
	
	/**
	 * ����json�ַ���
	 * @param action
	 * @param paramObj
	 * @param uuid
	 * @return
	 */
	private String createJSON(String action, JSONObject paramObj, String uuid) {
		JSONObject returnJson = new JSONObject();
		returnJson.put("action", action);
		returnJson.put("params", paramObj);
		returnJson.put("uuid", uuid);
		returnJson.put("auth", "");
		return returnJson.toJSONString();
	}
	
	/**
	 * ����model
	 * @param model
	 * @param current
	 * @param voltage
	 * @param power
	 */
	private void setModel(Model model, String current, String voltage, String power) {
		model.addAttribute("current", current + "mA");
		model.addAttribute("voltage", voltage + "V");
		model.addAttribute("power", power + "W");
	}
	
	/**
	 * ���ÿ���model
	 * @param model
	 * @param switchStatus
	 */
	private void setSwitchModel(Model model, String switchStatus) {
		while(switchStatus.length() < 4) {
			switchStatus = "0" + switchStatus;
		}
		statusStr = switchStatus;
		char[] switchArray = switchStatus.toCharArray();
		model.addAttribute("switch3", switchArray[0]=='0' ? "�ر�" : "��");
		model.addAttribute("switch2", switchArray[1]=='0' ? "�ر�" : "��");
		model.addAttribute("switch1", switchArray[2]=='0' ? "�ر�" : "��");
		model.addAttribute("mainSwitch", switchArray[3]=='0' ? "�ر�" : "��");
	}
	
	/**
	 * ��ʱ
	 * @param secds
	 */
	private void sleep(long secds) {
		try {
			Thread.sleep(secds);
		} catch (InterruptedException e) {
			log.error("��ʱ�쳣", e);
		}
	}
}
