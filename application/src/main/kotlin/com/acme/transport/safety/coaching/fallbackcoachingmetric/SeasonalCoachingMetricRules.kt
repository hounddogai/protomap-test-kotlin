package com.acme.transport.safety.coaching.fallbackcoachingmetric

/** A rule a seasonal coaching metric must pass. Check returns its problems, or none. */
interface SeasonalCoachingMetricRule {
    fun check(item: SeasonalCoachingMetric): MutableList<String>
}

data class SeasonalCoachingMetricRequiredLabelRule(var message: String) : SeasonalCoachingMetricRule {
    override fun check(item: SeasonalCoachingMetric): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCoachingMetricPinnedGuardRule(var limit: Int) : SeasonalCoachingMetricRule {
    override fun check(item: SeasonalCoachingMetric): MutableList<String> {
        if (item.pinned && item.odometer > limit) {
            return mutableListOf<String>("odometer ${item.odometer} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCoachingMetricCodeLengthRule(var maxLength: Int) : SeasonalCoachingMetricRule {
    override fun check(item: SeasonalCoachingMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalCoachingMetricCategoryLengthRule(var maxLength: Int) : SeasonalCoachingMetricRule {
    override fun check(item: SeasonalCoachingMetric): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal coaching metric. */
data class SeasonalCoachingMetricValidator(var rules: MutableList<SeasonalCoachingMetricRule>) {
    fun validate(item: SeasonalCoachingMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalCoachingMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalCoachingMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalCoachingMetricValidator(): SeasonalCoachingMetricValidator {
    return SeasonalCoachingMetricValidator(
        mutableListOf<SeasonalCoachingMetricRule>(
            SeasonalCoachingMetricRequiredLabelRule("seasonal coaching metric needs a value"),
            SeasonalCoachingMetricPinnedGuardRule(32),
            SeasonalCoachingMetricCodeLengthRule(3600),
            SeasonalCoachingMetricCategoryLengthRule(64),
        ),
    )
}
