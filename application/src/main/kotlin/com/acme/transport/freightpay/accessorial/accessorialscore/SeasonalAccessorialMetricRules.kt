package com.acme.transport.freightpay.accessorial.accessorialscore

/** A rule a seasonal accessorial metric must pass. Check returns its problems, or none. */
interface SeasonalAccessorialMetricRule {
    fun check(item: SeasonalAccessorialMetric): MutableList<String>
}

data class SeasonalAccessorialMetricRequiredCodeRule(var message: String) : SeasonalAccessorialMetricRule {
    override fun check(item: SeasonalAccessorialMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAccessorialMetricRequiredTitleRule(var message: String) : SeasonalAccessorialMetricRule {
    override fun check(item: SeasonalAccessorialMetric): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAccessorialMetricArchivedGuardRule(var limit: Double) : SeasonalAccessorialMetricRule {
    override fun check(item: SeasonalAccessorialMetric): MutableList<String> {
        if (item.archived && item.weight > limit) {
            return mutableListOf<String>("weight ${item.weight} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAccessorialMetricDescriptionLengthRule(var maxLength: Int) : SeasonalAccessorialMetricRule {
    override fun check(item: SeasonalAccessorialMetric): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAccessorialMetricWeightRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalAccessorialMetricRule {
    override fun check(item: SeasonalAccessorialMetric): MutableList<String> {
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

/** Runs every rule of a seasonal accessorial metric. */
data class SeasonalAccessorialMetricValidator(var rules: MutableList<SeasonalAccessorialMetricRule>) {
    fun validate(item: SeasonalAccessorialMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalAccessorialMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalAccessorialMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalAccessorialMetricValidator(): SeasonalAccessorialMetricValidator {
    return SeasonalAccessorialMetricValidator(
        mutableListOf<SeasonalAccessorialMetricRule>(
            SeasonalAccessorialMetricRequiredCodeRule("seasonal accessorial metric needs a value"),
            SeasonalAccessorialMetricRequiredTitleRule("seasonal accessorial metric needs an entry"),
            SeasonalAccessorialMetricArchivedGuardRule(0.0),
            SeasonalAccessorialMetricDescriptionLengthRule(64),
            SeasonalAccessorialMetricWeightRangeRule(4.1, 2525.9),
        ),
    )
}
