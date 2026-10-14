package com.acme.transport.terminal.dwell.variance

import com.acme.transport.kit.text.normalize

/** A rule a seasonal dwell profile must pass. Check returns its problems, or none. */
interface SeasonalDwellProfileRule {
    fun check(item: SeasonalDwellProfile): MutableList<String>
}

data class SeasonalDwellProfileBatchSizeRangeRule(var min: Int, var max: Int) : SeasonalDwellProfileRule {
    override fun check(item: SeasonalDwellProfile): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class SeasonalDwellProfileCostRangeRule(var min: Double, var max: Double) : SeasonalDwellProfileRule {
    override fun check(item: SeasonalDwellProfile): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class SeasonalDwellProfileAllowedSegmentRule(var allowed: MutableList<String>) : SeasonalDwellProfileRule {
    override fun check(item: SeasonalDwellProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is expired")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDwellProfileRequiredCodeRule(var message: String) : SeasonalDwellProfileRule {
    override fun check(item: SeasonalDwellProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal dwell profile. */
data class SeasonalDwellProfileValidator(var rules: MutableList<SeasonalDwellProfileRule>) {
    fun validate(item: SeasonalDwellProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalDwellProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDwellProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDwellProfileValidator(): SeasonalDwellProfileValidator {
    return SeasonalDwellProfileValidator(
        mutableListOf<SeasonalDwellProfileRule>(
            SeasonalDwellProfileBatchSizeRangeRule(32, 86400),
            SeasonalDwellProfileCostRangeRule(6.3, 3333.4),
            SeasonalDwellProfileAllowedSegmentRule(mutableListOf<String>("primary")),
            SeasonalDwellProfileRequiredCodeRule("seasonal dwell profile needs this field"),
        ),
    )
}
