package com.acme.transport.emissions.carbonreport.reportthreshold

/** A rule a adaptive carbon report threshold must pass. Check returns its problems, or none. */
interface AdaptiveCarbonReportThresholdRule {
    fun check(item: AdaptiveCarbonReportThreshold): MutableList<String>
}

data class AdaptiveCarbonReportThresholdRouteCodeLengthRule(
    var maxLength: Int,
) : AdaptiveCarbonReportThresholdRule {
    override fun check(item: AdaptiveCarbonReportThreshold): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCarbonReportThresholdBudgetRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveCarbonReportThresholdRule {
    override fun check(item: AdaptiveCarbonReportThreshold): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class AdaptiveCarbonReportThresholdTitleLengthRule(
    var maxLength: Int,
) : AdaptiveCarbonReportThresholdRule {
    override fun check(item: AdaptiveCarbonReportThreshold): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCarbonReportThresholdRequiredCodeRule(
    var message: String,
) : AdaptiveCarbonReportThresholdRule {
    override fun check(item: AdaptiveCarbonReportThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCarbonReportThresholdRequiredRouteCodeRule(
    var message: String,
) : AdaptiveCarbonReportThresholdRule {
    override fun check(item: AdaptiveCarbonReportThreshold): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive carbon report threshold. */
data class AdaptiveCarbonReportThresholdValidator(var rules: MutableList<AdaptiveCarbonReportThresholdRule>) {
    fun validate(item: AdaptiveCarbonReportThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveCarbonReportThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveCarbonReportThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveCarbonReportThresholdValidator(): AdaptiveCarbonReportThresholdValidator {
    return AdaptiveCarbonReportThresholdValidator(
        mutableListOf<AdaptiveCarbonReportThresholdRule>(
            AdaptiveCarbonReportThresholdRouteCodeLengthRule(1000),
            AdaptiveCarbonReportThresholdBudgetRangeRule(5.3, 2800.9),
            AdaptiveCarbonReportThresholdTitleLengthRule(86400),
            AdaptiveCarbonReportThresholdRequiredCodeRule("adaptive carbon report threshold needs a value"),
            AdaptiveCarbonReportThresholdRequiredRouteCodeRule("adaptive carbon report threshold needs a value"),
        ),
    )
}
