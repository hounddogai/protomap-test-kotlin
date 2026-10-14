package com.acme.transport.freightpay.dispute.checkpoint

/** A rule a strict dispute quota must pass. Check returns its problems, or none. */
interface StrictDisputeQuotaRule {
    fun check(item: StrictDisputeQuota): MutableList<String>
}

data class StrictDisputeQuotaOwnerIdLengthRule(var maxLength: Int) : StrictDisputeQuotaRule {
    override fun check(item: StrictDisputeQuota): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDisputeQuotaThresholdRangeRule(var min: Double, var max: Double) : StrictDisputeQuotaRule {
    override fun check(item: StrictDisputeQuota): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class StrictDisputeQuotaCodeLengthRule(var maxLength: Int) : StrictDisputeQuotaRule {
    override fun check(item: StrictDisputeQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDisputeQuotaRequiredCodeRule(var message: String) : StrictDisputeQuotaRule {
    override fun check(item: StrictDisputeQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDisputeQuotaMaximumRangeRule(var min: Double, var max: Double) : StrictDisputeQuotaRule {
    override fun check(item: StrictDisputeQuota): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class StrictDisputeQuotaCodePrefixRule(var prefix: String) : StrictDisputeQuotaRule {
    override fun check(item: StrictDisputeQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict dispute quota. */
data class StrictDisputeQuotaValidator(var rules: MutableList<StrictDisputeQuotaRule>) {
    fun validate(item: StrictDisputeQuota): MutableList<String> {
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

    fun requireValid(item: StrictDisputeQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDisputeQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDisputeQuotaValidator(): StrictDisputeQuotaValidator {
    return StrictDisputeQuotaValidator(
        mutableListOf<StrictDisputeQuotaRule>(
            StrictDisputeQuotaOwnerIdLengthRule(86400),
            StrictDisputeQuotaThresholdRangeRule(7.9, 1565.7),
            StrictDisputeQuotaCodeLengthRule(120),
            StrictDisputeQuotaRequiredCodeRule("strict dispute quota needs an entry"),
            StrictDisputeQuotaMaximumRangeRule(5.5, 3418.4),
            StrictDisputeQuotaCodePrefixRule("Q"),
        ),
    )
}
