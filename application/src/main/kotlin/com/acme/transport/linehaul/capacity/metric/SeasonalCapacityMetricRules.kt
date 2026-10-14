package com.acme.transport.linehaul.capacity.metric

/** A rule a seasonal capacity metric must pass. Check returns its problems, or none. */
interface SeasonalCapacityMetricRule {
    fun check(item: SeasonalCapacityMetric): MutableList<String>
}

data class SeasonalCapacityMetricScoreRangeRule(var min: Double, var max: Double) : SeasonalCapacityMetricRule {
    override fun check(item: SeasonalCapacityMetric): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCapacityMetricOdometerRangeRule(var min: Int, var max: Int) : SeasonalCapacityMetricRule {
    override fun check(item: SeasonalCapacityMetric): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCapacityMetricWindowDaysRangeRule(var min: Int, var max: Int) : SeasonalCapacityMetricRule {
    override fun check(item: SeasonalCapacityMetric): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class SeasonalCapacityMetricSourceLengthRule(var maxLength: Int) : SeasonalCapacityMetricRule {
    override fun check(item: SeasonalCapacityMetric): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCapacityMetricTitleLengthRule(var maxLength: Int) : SeasonalCapacityMetricRule {
    override fun check(item: SeasonalCapacityMetric): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCapacityMetricGraceDaysRangeRule(var min: Int, var max: Int) : SeasonalCapacityMetricRule {
    override fun check(item: SeasonalCapacityMetric): MutableList<String> {
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

/** Runs every rule of a seasonal capacity metric. */
data class SeasonalCapacityMetricValidator(var rules: MutableList<SeasonalCapacityMetricRule>) {
    fun validate(item: SeasonalCapacityMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalCapacityMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalCapacityMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalCapacityMetricValidator(): SeasonalCapacityMetricValidator {
    return SeasonalCapacityMetricValidator(
        mutableListOf<SeasonalCapacityMetricRule>(
            SeasonalCapacityMetricScoreRangeRule(4.4, 1813.5),
            SeasonalCapacityMetricOdometerRangeRule(64, 86400),
            SeasonalCapacityMetricWindowDaysRangeRule(80, 3600),
            SeasonalCapacityMetricSourceLengthRule(16),
            SeasonalCapacityMetricTitleLengthRule(1000),
            SeasonalCapacityMetricGraceDaysRangeRule(3600, 3600),
        ),
    )
}
