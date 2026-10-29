package com.ggzed.chat.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 金融理财相关工具集合。
 */
@Slf4j
@Component
public class FinanceTools {

    @Tool(description = "【强制要求】当用户询问理财收益、利息、能赚多少钱时，必须调用此工具进行精确计算。严禁大模型自己进行数学计算。")
    public String calculateProfit(
            @ToolParam(description = "本金金额，单位元") double principal,
            @ToolParam(description = "年化收益率，例如 2.5% 传 0.025") double rate,
            @ToolParam(description = "持有天数，从资料中获取") int days) {

        // 银行通常按 365 天计算年化
        double profit = principal * rate * (days / 365.0);
        return String.format("根据计算，本金 %.0f 元，年化 %.2f%%，持有 %d 天，预计到期收益为 %.2f 元。",
                principal, rate * 100, days, profit);
    }

    @Tool(description = "执行银行转账操作。高危操作，需人工审批。")
    public String transferMoney() {
        log.info("执行 transferMoney 方法");
        return "执行transferMoney方法";
    }

    @Tool(description = "执行理财产品购买扣款。当用户明确表示要购买理财时调用。")
    public String buyFinancialProduct(
            @ToolParam(description = "客户卡号") String accountNo,
            @ToolParam(description = "购买金额") double amount) {

        log.warn("[真实扣款执行] 正在从账户 {} 扣款 {} 元！", accountNo, amount);
        return "扣款成功";
    }
}
