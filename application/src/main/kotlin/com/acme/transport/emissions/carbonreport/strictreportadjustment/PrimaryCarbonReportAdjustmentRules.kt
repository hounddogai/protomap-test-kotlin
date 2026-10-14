package com.acme.transport.emissions.carbonreport.strictreportadjustment

/** A rule a primary carbon report adjustment must pass. Check returns its problems, or none. */
interface PrimaryCarbonReportAdjustmentRule {
    fun check(item: PrimaryCarbonReportAdjustment): MutableList<String>
}

data class PrimaryCarbonReportAdjustmentRequiredTitleRule(
    var message: String,
) : PrimaryCarbonReportAdjustmentRule {
    override fun check(item: PrimaryCarbonReportAdjustment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCarbonReportAdjustmentCodeLengthRule(var maxLength: Int) : PrimaryCarbonReportAdjustmentRule {
    override fun check(item: PrimaryCarbonReportAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCarbonReportAdjustmentExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryCarbonReportAdjustmentRule {
    override fun check(item: PrimaryCarbonReportAdjustment): MutableList<String> {
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

/** Runs every rule of a primary carbon report adjustment. */
data class PrimaryCarbonReportAdjustmentValidator(var rules: MutableList<PrimaryCarbonReportAdjustmentRule>) {
    fun validate(item: PrimaryCarbonReportAdjustment): MutableList<String> {
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

    fun requireValid(item: PrimaryCarbonReportAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCarbonReportAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCarbonReportAdjustmentValidator(): PrimaryCarbonReportAdjustmentValidator {
    return PrimaryCarbonReportAdjustmentValidator(
        mutableListOf<PrimaryCarbonReportAdjustmentRule>(
            PrimaryCarbonReportAdjustmentRequiredTitleRule("primary carbon report adjustment needs this field"),
            PrimaryCarbonReportAdjustmentCodeLengthRule(255),
            PrimaryCarbonReportAdjustmentExpiresAtRangeRule(80, 86400),
        ),
    )
}
