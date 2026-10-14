package com.acme.transport.fleetops.serviceplan.planthreshold

/** A rule a seasonal service plan threshold must pass. Check returns its problems, or none. */
interface SeasonalServicePlanThresholdRule {
    fun check(item: SeasonalServicePlanThreshold): MutableList<String>
}

data class SeasonalServicePlanThresholdLabelLengthRule(var maxLength: Int) : SeasonalServicePlanThresholdRule {
    override fun check(item: SeasonalServicePlanThreshold): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServicePlanThresholdRequiredCodeRule(
    var message: String,
) : SeasonalServicePlanThresholdRule {
    override fun check(item: SeasonalServicePlanThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServicePlanThresholdRetriesRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalServicePlanThresholdRule {
    override fun check(item: SeasonalServicePlanThreshold): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServicePlanThresholdRequiredLabelRule(
    var message: String,
) : SeasonalServicePlanThresholdRule {
    override fun check(item: SeasonalServicePlanThreshold): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServicePlanThresholdRouteCodeLengthRule(
    var maxLength: Int,
) : SeasonalServicePlanThresholdRule {
    override fun check(item: SeasonalServicePlanThreshold): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal service plan threshold. */
data class SeasonalServicePlanThresholdValidator(var rules: MutableList<SeasonalServicePlanThresholdRule>) {
    fun validate(item: SeasonalServicePlanThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalServicePlanThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalServicePlanThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalServicePlanThresholdValidator(): SeasonalServicePlanThresholdValidator {
    return SeasonalServicePlanThresholdValidator(
        mutableListOf<SeasonalServicePlanThresholdRule>(
            SeasonalServicePlanThresholdLabelLengthRule(32),
            SeasonalServicePlanThresholdRequiredCodeRule("seasonal service plan threshold needs an entry"),
            SeasonalServicePlanThresholdRetriesRangeRule(32, 86400),
            SeasonalServicePlanThresholdRequiredLabelRule("seasonal service plan threshold needs a value"),
            SeasonalServicePlanThresholdRouteCodeLengthRule(16),
        ),
    )
}
