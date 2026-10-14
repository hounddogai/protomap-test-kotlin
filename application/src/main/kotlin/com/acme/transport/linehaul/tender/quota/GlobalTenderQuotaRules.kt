package com.acme.transport.linehaul.tender.quota

import com.acme.transport.kit.text.normalize

/** A rule a global tender quota must pass. Check returns its problems, or none. */
interface GlobalTenderQuotaRule {
    fun check(item: GlobalTenderQuota): MutableList<String>
}

data class GlobalTenderQuotaAllowedCodeRule(var allowed: MutableList<String>) : GlobalTenderQuotaRule {
    override fun check(item: GlobalTenderQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTenderQuotaFloorValueRangeRule(var min: Double, var max: Double) : GlobalTenderQuotaRule {
    override fun check(item: GlobalTenderQuota): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTenderQuotaCodePrefixRule(var prefix: String) : GlobalTenderQuotaRule {
    override fun check(item: GlobalTenderQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTenderQuotaReviewerLengthRule(var maxLength: Int) : GlobalTenderQuotaRule {
    override fun check(item: GlobalTenderQuota): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTenderQuotaRequiredReviewerRule(var message: String) : GlobalTenderQuotaRule {
    override fun check(item: GlobalTenderQuota): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalTenderQuotaCodeLengthRule(var maxLength: Int) : GlobalTenderQuotaRule {
    override fun check(item: GlobalTenderQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global tender quota. */
data class GlobalTenderQuotaValidator(var rules: MutableList<GlobalTenderQuotaRule>) {
    fun validate(item: GlobalTenderQuota): MutableList<String> {
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

    fun requireValid(item: GlobalTenderQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTenderQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTenderQuotaValidator(): GlobalTenderQuotaValidator {
    return GlobalTenderQuotaValidator(
        mutableListOf<GlobalTenderQuotaRule>(
            GlobalTenderQuotaAllowedCodeRule(mutableListOf<String>("adaptive")),
            GlobalTenderQuotaFloorValueRangeRule(3.3, 1403.5),
            GlobalTenderQuotaCodePrefixRule("X"),
            GlobalTenderQuotaReviewerLengthRule(120),
            GlobalTenderQuotaRequiredReviewerRule("global tender quota needs an entry"),
            GlobalTenderQuotaCodeLengthRule(120),
        ),
    )
}
