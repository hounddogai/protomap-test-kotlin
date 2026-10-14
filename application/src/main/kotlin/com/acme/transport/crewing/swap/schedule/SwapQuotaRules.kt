package com.acme.transport.crewing.swap.schedule

/** A rule a swap quota must pass. Check returns its problems, or none. */
interface SwapQuotaRule {
    fun check(item: SwapQuota): MutableList<String>
}

data class SwapQuotaCodeLengthRule(var maxLength: Int) : SwapQuotaRule {
    override fun check(item: SwapQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapQuotaRequiredTitleRule(var message: String) : SwapQuotaRule {
    override fun check(item: SwapQuota): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapQuotaAmountRangeRule(var min: Double, var max: Double) : SwapQuotaRule {
    override fun check(item: SwapQuota): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a swap quota. */
data class SwapQuotaValidator(var rules: MutableList<SwapQuotaRule>) {
    fun validate(item: SwapQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: SwapQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapQuota>): MutableMap<String, MutableList<String>> {
        val report = mutableMapOf<String, MutableList<String>>()
        for (item in items) {
            val problems = validate(item)
            if (problems.size > 0) {
                report[item.code] = problems
            }
        }
        return report
    }
}

fun defaultSwapQuotaValidator(): SwapQuotaValidator {
    return SwapQuotaValidator(
        mutableListOf<SwapQuotaRule>(
            SwapQuotaCodeLengthRule(3600),
            SwapQuotaRequiredTitleRule("swap quota needs a value"),
            SwapQuotaAmountRangeRule(9.6, 1977.1),
        ),
    )
}
