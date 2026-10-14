package com.acme.transport.emissions.carbonreport.score

import com.acme.transport.kit.text.normalize

/** A rule a pending carbon report exception must pass. Check returns its problems, or none. */
interface PendingCarbonReportExceptionRule {
    fun check(item: PendingCarbonReportException): MutableList<String>
}

data class PendingCarbonReportExceptionAllowedCodeRule(
    var allowed: MutableList<String>,
) : PendingCarbonReportExceptionRule {
    override fun check(item: PendingCarbonReportException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class PendingCarbonReportExceptionRequiredTitleRule(
    var message: String,
) : PendingCarbonReportExceptionRule {
    override fun check(item: PendingCarbonReportException): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingCarbonReportExceptionFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : PendingCarbonReportExceptionRule {
    override fun check(item: PendingCarbonReportException): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a pending carbon report exception. */
data class PendingCarbonReportExceptionValidator(var rules: MutableList<PendingCarbonReportExceptionRule>) {
    fun validate(item: PendingCarbonReportException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingCarbonReportException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingCarbonReportException>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingCarbonReportExceptionValidator(): PendingCarbonReportExceptionValidator {
    return PendingCarbonReportExceptionValidator(
        mutableListOf<PendingCarbonReportExceptionRule>(
            PendingCarbonReportExceptionAllowedCodeRule(mutableListOf<String>()),
            PendingCarbonReportExceptionRequiredTitleRule("pending carbon report exception needs this field"),
            PendingCarbonReportExceptionFloorValueRangeRule(6.1, 2999.0),
        ),
    )
}
