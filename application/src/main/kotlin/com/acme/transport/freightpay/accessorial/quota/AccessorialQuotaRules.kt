package com.acme.transport.freightpay.accessorial.quota

/** A rule a accessorial quota must pass. Check returns its problems, or none. */
interface AccessorialQuotaRule {
    fun check(item: AccessorialQuota): MutableList<String>
}

data class AccessorialQuotaSegmentLengthRule(var maxLength: Int) : AccessorialQuotaRule {
    override fun check(item: AccessorialQuota): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialQuotaRequiredDriverIdRule(var message: String) : AccessorialQuotaRule {
    override fun check(item: AccessorialQuota): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialQuotaCodeLengthRule(var maxLength: Int) : AccessorialQuotaRule {
    override fun check(item: AccessorialQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialQuotaAmountRangeRule(var min: Double, var max: Double) : AccessorialQuotaRule {
    override fun check(item: AccessorialQuota): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialQuotaRequiredCodeRule(var message: String) : AccessorialQuotaRule {
    override fun check(item: AccessorialQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial quota. */
data class AccessorialQuotaValidator(var rules: MutableList<AccessorialQuotaRule>) {
    fun validate(item: AccessorialQuota): MutableList<String> {
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

    fun requireValid(item: AccessorialQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialQuotaValidator(): AccessorialQuotaValidator {
    return AccessorialQuotaValidator(
        mutableListOf<AccessorialQuotaRule>(
            AccessorialQuotaSegmentLengthRule(64),
            AccessorialQuotaRequiredDriverIdRule("accessorial quota needs this field"),
            AccessorialQuotaCodeLengthRule(16),
            AccessorialQuotaAmountRangeRule(9.5, 3953.8),
            AccessorialQuotaRequiredCodeRule("accessorial quota needs a value"),
        ),
    )
}
