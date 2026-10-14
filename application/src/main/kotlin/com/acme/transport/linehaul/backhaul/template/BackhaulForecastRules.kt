package com.acme.transport.linehaul.backhaul.template

/** A rule a backhaul forecast must pass. Check returns its problems, or none. */
interface BackhaulForecastRule {
    fun check(item: BackhaulForecast): MutableList<String>
}

data class BackhaulForecastRequiredTitleRule(var message: String) : BackhaulForecastRule {
    override fun check(item: BackhaulForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulForecastRequiredSegmentRule(var message: String) : BackhaulForecastRule {
    override fun check(item: BackhaulForecast): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulForecastRateRangeRule(var min: Double, var max: Double) : BackhaulForecastRule {
    override fun check(item: BackhaulForecast): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class BackhaulForecastWeightRangeRule(var min: Double, var max: Double) : BackhaulForecastRule {
    override fun check(item: BackhaulForecast): MutableList<String> {
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

data class BackhaulForecastTitleLengthRule(var maxLength: Int) : BackhaulForecastRule {
    override fun check(item: BackhaulForecast): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulForecastRequiredReviewerRule(var message: String) : BackhaulForecastRule {
    override fun check(item: BackhaulForecast): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul forecast. */
data class BackhaulForecastValidator(var rules: MutableList<BackhaulForecastRule>) {
    fun validate(item: BackhaulForecast): MutableList<String> {
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

    fun requireValid(item: BackhaulForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulForecastValidator(): BackhaulForecastValidator {
    return BackhaulForecastValidator(
        mutableListOf<BackhaulForecastRule>(
            BackhaulForecastRequiredTitleRule("backhaul forecast needs a value"),
            BackhaulForecastRequiredSegmentRule("backhaul forecast needs an entry"),
            BackhaulForecastRateRangeRule(0.3, 2361.3),
            BackhaulForecastWeightRangeRule(1.9, 2411.9),
            BackhaulForecastTitleLengthRule(80),
            BackhaulForecastRequiredReviewerRule("backhaul forecast needs an entry"),
        ),
    )
}
