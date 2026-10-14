package com.acme.transport.lastmile.redelivery.request

/** A rule a seasonal redelivery threshold must pass. Check returns its problems, or none. */
interface SeasonalRedeliveryThresholdRule {
    fun check(item: SeasonalRedeliveryThreshold): MutableList<String>
}

data class SeasonalRedeliveryThresholdRequiredDockRule(var message: String) : SeasonalRedeliveryThresholdRule {
    override fun check(item: SeasonalRedeliveryThreshold): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRedeliveryThresholdDockLengthRule(var maxLength: Int) : SeasonalRedeliveryThresholdRule {
    override fun check(item: SeasonalRedeliveryThreshold): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRedeliveryThresholdWeightRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalRedeliveryThresholdRule {
    override fun check(item: SeasonalRedeliveryThreshold): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a seasonal redelivery threshold. */
data class SeasonalRedeliveryThresholdValidator(var rules: MutableList<SeasonalRedeliveryThresholdRule>) {
    fun validate(item: SeasonalRedeliveryThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalRedeliveryThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRedeliveryThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRedeliveryThresholdValidator(): SeasonalRedeliveryThresholdValidator {
    return SeasonalRedeliveryThresholdValidator(
        mutableListOf<SeasonalRedeliveryThresholdRule>(
            SeasonalRedeliveryThresholdRequiredDockRule("seasonal redelivery threshold needs this field"),
            SeasonalRedeliveryThresholdDockLengthRule(64),
            SeasonalRedeliveryThresholdWeightRangeRule(8.8, 2814.3),
        ),
    )
}
