package com.acme.transport.crewing.restbreak.breaksummary

/** A rule a seasonal rest break estimate must pass. Check returns its problems, or none. */
interface SeasonalRestBreakEstimateRule {
    fun check(item: SeasonalRestBreakEstimate): MutableList<String>
}

data class SeasonalRestBreakEstimateCapacityRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalRestBreakEstimateRule {
    override fun check(item: SeasonalRestBreakEstimate): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class SeasonalRestBreakEstimateLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalRestBreakEstimateRule {
    override fun check(item: SeasonalRestBreakEstimate): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRestBreakEstimateSummaryLengthRule(var maxLength: Int) : SeasonalRestBreakEstimateRule {
    override fun check(item: SeasonalRestBreakEstimate): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal rest break estimate. */
data class SeasonalRestBreakEstimateValidator(var rules: MutableList<SeasonalRestBreakEstimateRule>) {
    fun validate(item: SeasonalRestBreakEstimate): MutableList<String> {
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

    fun requireValid(item: SeasonalRestBreakEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRestBreakEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRestBreakEstimateValidator(): SeasonalRestBreakEstimateValidator {
    return SeasonalRestBreakEstimateValidator(
        mutableListOf<SeasonalRestBreakEstimateRule>(
            SeasonalRestBreakEstimateCapacityRangeRule(16, 3600),
            SeasonalRestBreakEstimateLoadKgRangeRule(2.0, 4905.9),
            SeasonalRestBreakEstimateSummaryLengthRule(1000),
        ),
    )
}
