package com.acme.transport.terminal.sortation.sortationvariance

import com.acme.transport.kit.text.normalize

/** A rule a seasonal sortation variance must pass. Check returns its problems, or none. */
interface SeasonalSortationVarianceRule {
    fun check(item: SeasonalSortationVariance): MutableList<String>
}

data class SeasonalSortationVarianceVisibleGuardRule(var limit: Double) : SeasonalSortationVarianceRule {
    override fun check(item: SeasonalSortationVariance): MutableList<String> {
        if (item.visible && item.ratio > limit) {
            return mutableListOf<String>("ratio ${item.ratio} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalSortationVarianceCeilingRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalSortationVarianceRule {
    override fun check(item: SeasonalSortationVariance): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalSortationVarianceAllowedCodeRule(
    var allowed: MutableList<String>,
) : SeasonalSortationVarianceRule {
    override fun check(item: SeasonalSortationVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalSortationVarianceRetriesRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalSortationVarianceRule {
    override fun check(item: SeasonalSortationVariance): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalSortationVarianceWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalSortationVarianceRule {
    override fun check(item: SeasonalSortationVariance): MutableList<String> {
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

/** Runs every rule of a seasonal sortation variance. */
data class SeasonalSortationVarianceValidator(var rules: MutableList<SeasonalSortationVarianceRule>) {
    fun validate(item: SeasonalSortationVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalSortationVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalSortationVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalSortationVarianceValidator(): SeasonalSortationVarianceValidator {
    return SeasonalSortationVarianceValidator(
        mutableListOf<SeasonalSortationVarianceRule>(
            SeasonalSortationVarianceVisibleGuardRule(7.7),
            SeasonalSortationVarianceCeilingRangeRule(7.8, 3065.8),
            SeasonalSortationVarianceAllowedCodeRule(mutableListOf<String>()),
            SeasonalSortationVarianceRetriesRangeRule(16, 255),
            SeasonalSortationVarianceWindowDaysRangeRule(255, 3600),
        ),
    )
}
