package com.acme.transport.emissions.carbonreport.claim

import com.acme.transport.kit.text.normalize

/** A rule a carbon report claim must pass. Check returns its problems, or none. */
interface CarbonReportClaimRule {
    fun check(item: CarbonReportClaim): MutableList<String>
}

data class CarbonReportClaimCodeLengthRule(var maxLength: Int) : CarbonReportClaimRule {
    override fun check(item: CarbonReportClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportClaimGraceDaysRangeRule(var min: Int, var max: Int) : CarbonReportClaimRule {
    override fun check(item: CarbonReportClaim): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class CarbonReportClaimRequiredCodeRule(var message: String) : CarbonReportClaimRule {
    override fun check(item: CarbonReportClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportClaimAllowedLabelRule(var allowed: MutableList<String>) : CarbonReportClaimRule {
    override fun check(item: CarbonReportClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report claim. */
data class CarbonReportClaimValidator(var rules: MutableList<CarbonReportClaimRule>) {
    fun validate(item: CarbonReportClaim): MutableList<String> {
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

    fun requireValid(item: CarbonReportClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportClaimValidator(): CarbonReportClaimValidator {
    return CarbonReportClaimValidator(
        mutableListOf<CarbonReportClaimRule>(
            CarbonReportClaimCodeLengthRule(32),
            CarbonReportClaimGraceDaysRangeRule(86400, 86400),
            CarbonReportClaimRequiredCodeRule("carbon report claim needs a value"),
            CarbonReportClaimAllowedLabelRule(mutableListOf<String>()),
        ),
    )
}
