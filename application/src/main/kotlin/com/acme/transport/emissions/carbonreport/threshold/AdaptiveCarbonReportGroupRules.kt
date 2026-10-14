package com.acme.transport.emissions.carbonreport.threshold

/** A rule a adaptive carbon report group must pass. Check returns its problems, or none. */
interface AdaptiveCarbonReportGroupRule {
    fun check(item: AdaptiveCarbonReportGroup): MutableList<String>
}

data class AdaptiveCarbonReportGroupCarrierLengthRule(var maxLength: Int) : AdaptiveCarbonReportGroupRule {
    override fun check(item: AdaptiveCarbonReportGroup): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCarbonReportGroupRequiredTitleRule(var message: String) : AdaptiveCarbonReportGroupRule {
    override fun check(item: AdaptiveCarbonReportGroup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCarbonReportGroupFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveCarbonReportGroupRule {
    override fun check(item: AdaptiveCarbonReportGroup): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a adaptive carbon report group. */
data class AdaptiveCarbonReportGroupValidator(var rules: MutableList<AdaptiveCarbonReportGroupRule>) {
    fun validate(item: AdaptiveCarbonReportGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveCarbonReportGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveCarbonReportGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveCarbonReportGroupValidator(): AdaptiveCarbonReportGroupValidator {
    return AdaptiveCarbonReportGroupValidator(
        mutableListOf<AdaptiveCarbonReportGroupRule>(
            AdaptiveCarbonReportGroupCarrierLengthRule(1000),
            AdaptiveCarbonReportGroupRequiredTitleRule("adaptive carbon report group needs a value"),
            AdaptiveCarbonReportGroupFuelLitersRangeRule(1.3, 4659.6),
        ),
    )
}
