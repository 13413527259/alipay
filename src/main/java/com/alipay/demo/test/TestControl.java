package com.alipay.demo.test;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeWapPayRequest;

@Controller
public class TestControl {

	@Value("${alipay.APP_ID}")
	private String APP_ID;
	@Value("${alipay.CHARSET}")
	private String CHARSET;
	@Value("${alipay.FORMAT}")
	private String FORMAT;
	@Value("${alipay.SIGNTYPE}")
	private String SIGNTYPE;
	@Value("${alipay.APP_PRIVATE_KEY}")
	private String APP_PRIVATE_KEY;
	@Value("${alipay.ALIPAY_PUBLIC_KEY}")
	private String ALIPAY_PUBLIC_KEY;
	@Value("${alipay.alipay_url}")
	private String alipay_url;
	@Value("${alipay.notify_url}")
	private String notify_url;
	@Value("${alipay.return_url}")
	private String return_url;

	@GetMapping("/testpay")
	public void testpay(@RequestParam("body") String body, @RequestParam("title") String title,
			@RequestParam("amount") String amount, @RequestParam("order") String order,
			HttpServletResponse httpResponse) throws ServletException, IOException {
		AlipayClient alipayClient = new DefaultAlipayClient(alipay_url, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET,
				ALIPAY_PUBLIC_KEY, SIGNTYPE); // 获得初始化的AlipayClient
		AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();// 创建API对应的request
		alipayRequest.setReturnUrl(return_url);
		alipayRequest.setNotifyUrl(notify_url);// 在公共参数中设置回跳和通知地址
		AlipayTradeWapPayModel payModel = new AlipayTradeWapPayModel();
		payModel.setBody(body);// 描述
		payModel.setSubject(title);// 标题
		payModel.setOutTradeNo(order);// 订单号
		payModel.setTotalAmount(amount);// 钱
		payModel.setSellerId("208812345671234567");
		payModel.setTimeExpire("90m");
		payModel.setProductCode("QUICK_WAP_WAY");
		payModel.setGoodsType("1");
		alipayRequest.setBizModel(payModel);
		String form = "";
		try {
			form = alipayClient.pageExecute(alipayRequest).getBody(); // 调用SDK生成表单
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		httpResponse.setContentType("text/html;charset=" + CHARSET);
		httpResponse.getWriter().write(form);// 直接将完整的表单html输出到页面
		httpResponse.getWriter().flush();
		httpResponse.getWriter().close();
	}

}
