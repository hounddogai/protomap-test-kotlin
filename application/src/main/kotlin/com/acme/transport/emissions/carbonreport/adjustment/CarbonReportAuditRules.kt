package com.acme.transport.emissions.carbonreport.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a carbon report audit must pass. Check returns its problems, or none. */
interface CarbonReportAuditRule {
    fun check(item: CarbonReportAudit): MutableList<String>
}

data class CarbonReportAuditExpiresAtRangeRule(var min: Int, var max: Int) : CarbonReportAuditRule {
    override fun check(item: CarbonReportAudit): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class CarbonReportAuditAllowedLaneRule(var allowed: MutableList<String>) : CarbonReportAuditRule {
    override fun check(item: CarbonReportAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is too long")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportAuditLaneLengthRule(var maxLength: Int) : CarbonReportAuditRule {
    override fun check(item: CarbonReportAudit): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportAuditRequiredCodeRule(var message: String) : CarbonReportAuditRule {
    override fun check(item: CarbonReportAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportAuditOdometerRangeRule(var min: Int, var max: Int) : CarbonReportAuditRule {
    override fun check(item: CarbonReportAudit): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportAuditLabelLengthRule(var maxLength: Int) : CarbonReportAuditRule {
    override fun check(item: CarbonReportAudit): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report audit. */
data class CarbonReportAuditValidator(var rules: MutableList<CarbonReportAuditRule>) {
    fun validate(item: CarbonReportAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CarbonReportAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportAuditValidator(): CarbonReportAuditValidator {
    return CarbonReportAuditValidator(
        mutableListOf<CarbonReportAuditRule>(
            CarbonReportAuditExpiresAtRangeRule(16, 255),
            CarbonReportAuditAllowedLaneRule(mutableListOf<String>("global", "scheduled")),
            CarbonReportAuditLaneLengthRule(32),
            CarbonReportAuditRequiredCodeRule("carbon report audit needs a value"),
            CarbonReportAuditOdometerRangeRule(255, 255),
            CarbonReportAuditLabelLengthRule(120),
        ),
    )
}
