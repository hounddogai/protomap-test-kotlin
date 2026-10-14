package com.acme.transport.freightpay.audit.claim

/** A rule a adaptive audit batch must pass. Check returns its problems, or none. */
interface AdaptiveAuditBatchRule {
    fun check(item: AdaptiveAuditBatch): MutableList<String>
}

data class AdaptiveAuditBatchRequiredChannelRule(var message: String) : AdaptiveAuditBatchRule {
    override fun check(item: AdaptiveAuditBatch): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAuditBatchPalletsRangeRule(var min: Int, var max: Int) : AdaptiveAuditBatchRule {
    override fun check(item: AdaptiveAuditBatch): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAuditBatchRatioRangeRule(var min: Double, var max: Double) : AdaptiveAuditBatchRule {
    override fun check(item: AdaptiveAuditBatch): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class AdaptiveAuditBatchRequiredCodeRule(var message: String) : AdaptiveAuditBatchRule {
    override fun check(item: AdaptiveAuditBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAuditBatchCodePrefixRule(var prefix: String) : AdaptiveAuditBatchRule {
    override fun check(item: AdaptiveAuditBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAuditBatchCodeLengthRule(var maxLength: Int) : AdaptiveAuditBatchRule {
    override fun check(item: AdaptiveAuditBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive audit batch. */
data class AdaptiveAuditBatchValidator(var rules: MutableList<AdaptiveAuditBatchRule>) {
    fun validate(item: AdaptiveAuditBatch): MutableList<String> {
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

    fun requireValid(item: AdaptiveAuditBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveAuditBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveAuditBatchValidator(): AdaptiveAuditBatchValidator {
    return AdaptiveAuditBatchValidator(
        mutableListOf<AdaptiveAuditBatchRule>(
            AdaptiveAuditBatchRequiredChannelRule("adaptive audit batch needs an entry"),
            AdaptiveAuditBatchPalletsRangeRule(32, 3600),
            AdaptiveAuditBatchRatioRangeRule(1.1, 469.4),
            AdaptiveAuditBatchRequiredCodeRule("adaptive audit batch needs an entry"),
            AdaptiveAuditBatchCodePrefixRule("ACM"),
            AdaptiveAuditBatchCodeLengthRule(120),
        ),
    )
}
