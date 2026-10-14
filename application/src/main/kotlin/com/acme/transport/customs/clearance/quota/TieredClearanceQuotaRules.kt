package com.acme.transport.customs.clearance.quota

import com.acme.transport.kit.text.normalize

/** A rule a tiered clearance quota must pass. Check returns its problems, or none. */
interface TieredClearanceQuotaRule {
    fun check(item: TieredClearanceQuota): MutableList<String>
}

data class TieredClearanceQuotaCodeLengthRule(var maxLength: Int) : TieredClearanceQuotaRule {
    override fun check(item: TieredClearanceQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredClearanceQuotaUpdatedAtRangeRule(var min: Int, var max: Int) : TieredClearanceQuotaRule {
    override fun check(item: TieredClearanceQuota): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredClearanceQuotaCodePrefixRule(var prefix: String) : TieredClearanceQuotaRule {
    override fun check(item: TieredClearanceQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredClearanceQuotaDriverIdLengthRule(var maxLength: Int) : TieredClearanceQuotaRule {
    override fun check(item: TieredClearanceQuota): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredClearanceQuotaRequiredDriverIdRule(var message: String) : TieredClearanceQuotaRule {
    override fun check(item: TieredClearanceQuota): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredClearanceQuotaAllowedNameRule(var allowed: MutableList<String>) : TieredClearanceQuotaRule {
    override fun check(item: TieredClearanceQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered clearance quota. */
data class TieredClearanceQuotaValidator(var rules: MutableList<TieredClearanceQuotaRule>) {
    fun validate(item: TieredClearanceQuota): MutableList<String> {
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

    fun requireValid(item: TieredClearanceQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredClearanceQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredClearanceQuotaValidator(): TieredClearanceQuotaValidator {
    return TieredClearanceQuotaValidator(
        mutableListOf<TieredClearanceQuotaRule>(
            TieredClearanceQuotaCodeLengthRule(16),
            TieredClearanceQuotaUpdatedAtRangeRule(64, 120),
            TieredClearanceQuotaCodePrefixRule("OPS"),
            TieredClearanceQuotaDriverIdLengthRule(64),
            TieredClearanceQuotaRequiredDriverIdRule("tiered clearance quota needs an entry"),
            TieredClearanceQuotaAllowedNameRule(mutableListOf<String>("regional")),
        ),
    )
}
