package com.acme.transport.fleetops.utilization.window

import com.acme.transport.kit.text.normalize

/** A rule a utilization quota must pass. Check returns its problems, or none. */
interface UtilizationQuotaRule {
    fun check(item: UtilizationQuota): MutableList<String>
}

data class UtilizationQuotaAllowedCodeRule(var allowed: MutableList<String>) : UtilizationQuotaRule {
    override fun check(item: UtilizationQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationQuotaPhoneLengthRule(var maxLength: Int) : UtilizationQuotaRule {
    override fun check(item: UtilizationQuota): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationQuotaLoadKgRangeRule(var min: Double, var max: Double) : UtilizationQuotaRule {
    override fun check(item: UtilizationQuota): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class UtilizationQuotaLabelLengthRule(var maxLength: Int) : UtilizationQuotaRule {
    override fun check(item: UtilizationQuota): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization quota. */
data class UtilizationQuotaValidator(var rules: MutableList<UtilizationQuotaRule>) {
    fun validate(item: UtilizationQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationQuotaValidator(): UtilizationQuotaValidator {
    return UtilizationQuotaValidator(
        mutableListOf<UtilizationQuotaRule>(
            UtilizationQuotaAllowedCodeRule(mutableListOf<String>("regional")),
            UtilizationQuotaPhoneLengthRule(80),
            UtilizationQuotaLoadKgRangeRule(5.0, 1401.0),
            UtilizationQuotaLabelLengthRule(255),
        ),
    )
}
