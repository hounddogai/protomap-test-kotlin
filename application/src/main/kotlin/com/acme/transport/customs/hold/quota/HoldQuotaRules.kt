package com.acme.transport.customs.hold.quota

import com.acme.transport.kit.text.normalize

/** A rule a hold quota must pass. Check returns its problems, or none. */
interface HoldQuotaRule {
    fun check(item: HoldQuota): MutableList<String>
}

data class HoldQuotaDiscountRangeRule(var min: Double, var max: Double) : HoldQuotaRule {
    override fun check(item: HoldQuota): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class HoldQuotaNameLengthRule(var maxLength: Int) : HoldQuotaRule {
    override fun check(item: HoldQuota): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldQuotaRequiredCodeRule(var message: String) : HoldQuotaRule {
    override fun check(item: HoldQuota): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldQuotaBatchSizeRangeRule(var min: Int, var max: Int) : HoldQuotaRule {
    override fun check(item: HoldQuota): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HoldQuotaAllowedDriverIdRule(var allowed: MutableList<String>) : HoldQuotaRule {
    override fun check(item: HoldQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold quota. */
data class HoldQuotaValidator(var rules: MutableList<HoldQuotaRule>) {
    fun validate(item: HoldQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HoldQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldQuotaValidator(): HoldQuotaValidator {
    return HoldQuotaValidator(
        mutableListOf<HoldQuotaRule>(
            HoldQuotaDiscountRangeRule(0.9, 121.7),
            HoldQuotaNameLengthRule(64),
            HoldQuotaRequiredCodeRule("hold quota needs an entry"),
            HoldQuotaBatchSizeRangeRule(16, 86400),
            HoldQuotaAllowedDriverIdRule(mutableListOf<String>("lenient")),
        ),
    )
}
