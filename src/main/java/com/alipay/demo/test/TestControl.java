package com.alipay.demo.test;

import java.io.IOException;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayDataDataserviceBillDownloadurlQueryModel;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradeFastpayRefundQueryModel;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayDataDataserviceBillDownloadurlQueryRequest;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayDataDataserviceBillDownloadurlQueryResponse;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;

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
	@Value("${alipay.APP_PUBLIC_KEY}")
	private String APP_PUBLIC_KEY;
	@Value("${alipay.ALIPAY_PUBLIC_KEY}")
	private String ALIPAY_PUBLIC_KEY;
	@Value("${alipay.alipay_url}")
	private String alipay_url;
	@Value("${alipay.notify_url}")
	private String notify_url;
	@Value("${alipay.return_url}")
	private String return_url;

	/**
	 * 手机网站支付
	 * @param title
	 * @param amount
	 * @param order
	 * @param httpResponse
	 * @throws ServletException
	 * @throws IOException
	 */
	@GetMapping("/testpay")
	public void testpay(@RequestParam("title") String title, @RequestParam("amount") String amount,
			@RequestParam("order") String order, HttpServletResponse httpResponse)
			throws ServletException, IOException {
		// 获得初始化的AlipayClient
		AlipayClient alipayClient = new DefaultAlipayClient(alipay_url, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET,
				ALIPAY_PUBLIC_KEY, SIGNTYPE);
		AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();// 创建API对应的request
		alipayRequest.setReturnUrl(return_url);
		alipayRequest.setNotifyUrl(notify_url);// 在公共参数中设置回跳和通知地址
		AlipayTradeWapPayModel payModel = new AlipayTradeWapPayModel();
		payModel.setSubject(title);// 标题
		payModel.setOutTradeNo(order);// 订单号
		payModel.setTotalAmount(amount);// 钱
		payModel.setProductCode("QUICK_WAP_WAY");
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

	/**
	 * 异步通知
	 * @param map
	 * @return
	 * @throws AlipayApiException
	 */
	@PostMapping("/notify")
	@ResponseBody
	public String notify(@RequestParam Map<String, String> map) throws AlipayApiException {
		System.out.println("******************支付宝异步通知*********************");
		TreeSet<String> keySet = new TreeSet<>(map.keySet());
		keySet.comparator();
		for (String key : keySet) {
			System.out.println(key + "：" + map.get(key));
		}
		boolean sign = AlipaySignature.rsaCheckV1(map, ALIPAY_PUBLIC_KEY, CHARSET, SIGNTYPE);
		if (sign) {
			System.out.println("验签成功！！！");
		} else {
			System.out.println("验签失败！！！");
		}
		System.out.println("******************支付宝异步通知  end*********************");
		return "success";
	}

	/**
	 * 同步跳转
	 * @param map
	 * @return
	 */
	@GetMapping("/sync")
	@ResponseBody
	public Map<String, Object> sync(@RequestParam Map<String, Object> map) {
		System.out.println("******************支付宝同步回调*********************");
		for (String key : map.keySet()) {
			System.out.println(key + "：" + map.get(key));
		}
		System.out.println("******************支付宝同步回调  end*********************");
		return map;
	}

	/**
	 * 统一订单查询
	 * @param queryModel
	 * @return
	 * @throws AlipayApiException
	 */
	@GetMapping("/query")
	@ResponseBody
	public String query(AlipayTradeQueryModel queryModel) throws AlipayApiException {
		// 获得初始化的AlipayClient
		AlipayClient alipayClient = new DefaultAlipayClient(alipay_url, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET,
				ALIPAY_PUBLIC_KEY, SIGNTYPE);
		AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
		request.setBizModel(queryModel);
		AlipayTradeQueryResponse response = alipayClient.execute(request);
		return response.getBody();
	}

	/**
	 * 统一关闭订单
	 * 未支付前
	 * @param closeModel
	 * @return
	 * @throws AlipayApiException
	 */
	@GetMapping("/close")
	@ResponseBody
	public String close(AlipayTradeCloseModel closeModel) throws AlipayApiException {
		// 获得初始化的AlipayClient
		AlipayClient alipayClient = new DefaultAlipayClient(alipay_url, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET,
				ALIPAY_PUBLIC_KEY, SIGNTYPE);
		AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
		request.setBizModel(closeModel);
		AlipayTradeCloseResponse response = alipayClient.execute(request);
		return response.getBody();
	}

	/**
	 * 统一退款
	 * @param refundModel
	 * @return
	 * @throws AlipayApiException
	 */
	@GetMapping("/refund")
	@ResponseBody
	public String refund(AlipayTradeRefundModel refundModel) throws AlipayApiException {
		// 获得初始化的AlipayClient
		AlipayClient alipayClient = new DefaultAlipayClient(alipay_url, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET,
				ALIPAY_PUBLIC_KEY, SIGNTYPE);
		AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
		request.setBizModel(refundModel);
		AlipayTradeRefundResponse response = alipayClient.execute(request);
		return response.getBody();
	}

	/**
	 * 统一退款查询
	 * @param refundQueryModel
	 * @return
	 * @throws AlipayApiException
	 */
	@GetMapping("/refund/query")
	@ResponseBody
	public String refundQuery(AlipayTradeFastpayRefundQueryModel refundQueryModel) throws AlipayApiException {
		// 获得初始化的AlipayClient
		AlipayClient alipayClient = new DefaultAlipayClient(alipay_url, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET,
				ALIPAY_PUBLIC_KEY, SIGNTYPE);
		AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
		request.setBizModel(refundQueryModel);
		AlipayTradeFastpayRefundQueryResponse response = alipayClient.execute(request);
		return response.getBody();
	}
	/**
	 * 扫码支付
	 * @param precreateModel
	 * @param resp
	 * @return
	 * @throws AlipayApiException
	 * @throws IOException
	 */
	@GetMapping("/scanpay")
	public String scanPay(AlipayTradePrecreateModel precreateModel,HttpServletResponse resp) throws AlipayApiException, IOException {
		// 获得初始化的AlipayClient
		AlipayClient alipayClient = new DefaultAlipayClient(alipay_url, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET,
				ALIPAY_PUBLIC_KEY, SIGNTYPE);
		AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
		request.setBizModel(precreateModel);
		AlipayTradePrecreateResponse response = alipayClient.execute(request);
		resp.getWriter().write(response.getBody());
		return null;
	}
	
	/**
	 * 统一账单下载
	 * 使用的是沙箱测试，所以获取到的只是账单模板，想要下载获取账单数据，需使用正式环境测试； 
	 * @param billModel
	 * @param resp
	 * @return
	 * @throws AlipayApiException
	 * @throws IOException
	 */
	@GetMapping("/bill")
	@ResponseBody
	public String bill(AlipayDataDataserviceBillDownloadurlQueryModel billModel,HttpServletResponse resp) throws AlipayApiException, IOException {
		 //账单时间：日账单格式为yyyy-MM-dd，月账单格式为yyyy-MM。必须是前一天或者上一个月，不能当天当月
		billModel.setBillDate("2018-08-09");
		billModel.setBillType("signcustomer");
		// 获得初始化的AlipayClient
		AlipayClient alipayClient = new DefaultAlipayClient(alipay_url, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET,
				ALIPAY_PUBLIC_KEY, SIGNTYPE);
		AlipayDataDataserviceBillDownloadurlQueryRequest   request = new AlipayDataDataserviceBillDownloadurlQueryRequest ();
		request.setBizModel(billModel);
		AlipayDataDataserviceBillDownloadurlQueryResponse response = alipayClient.execute(request);
		resp.getWriter().write(response.getBody());
		return null;
	}
	
	/**
	 * 电脑网站支付
	 * @param billModel
	 * @param resp
	 * @return
	 * @throws AlipayApiException
	 * @throws IOException
	 */
	@GetMapping("/pagepay")
	public void pagePay(AlipayTradePagePayModel pageModel,HttpServletResponse resp) throws AlipayApiException, IOException {
		pageModel.setProductCode("FAST_INSTANT_TRADE_PAY");
		// 获得初始化的AlipayClient
		AlipayClient alipayClient = new DefaultAlipayClient(alipay_url, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET,
				ALIPAY_PUBLIC_KEY, SIGNTYPE);
		AlipayTradePagePayRequest   request = new AlipayTradePagePayRequest ();
		request.setNotifyUrl(notify_url);
		request.setReturnUrl(return_url);
		request.setBizModel(pageModel);
		AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
		resp.setContentType("text/html;charset=" + CHARSET);
		resp.getWriter().write(response.getBody());// 直接将完整的表单html输出到页面
		resp.getWriter().flush();
		resp.getWriter().close();
	}

}
