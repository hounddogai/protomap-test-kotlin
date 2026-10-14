package com.acme.transport.fleetops.defect.defectreview

import com.acme.transport.kit.text.normalize

/** A rule a legacy defect variance must pass. Check returns its problems, or none. */
interface LegacyDefectVarianceRule {
    fun check(item: LegacyDefectVariance): MutableList<String>
}

data class LegacyDefectVarianceAllowedTitleRule(var allowed: MutableList<String>) : LegacyDefectVarianceRule {
    override fun check(item: LegacyDefectVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is stale")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDefectVarianceRequiredTitleRule(var message: String) : LegacyDefectVarianceRule {
    override fun check(item: LegacyDefectVariance): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDefectVarianceDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : LegacyDefectVarianceRule {
    override fun check(item: LegacyDefectVariance): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a legacy defect variance. */
data class LegacyDefectVarianceValidator(var rules: MutableList<LegacyDefectVarianceRule>) {
    fun validate(item: LegacyDefectVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyDefectVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDefectVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDefectVarianceValidator(): LegacyDefectVarianceValidator {
    return LegacyDefectVarianceValidator(
        mutableListOf<LegacyDefectVarianceRule>(
            LegacyDefectVarianceAllowedTitleRule(mutableListOf<String>("tiered", "tiered")),
            LegacyDefectVarianceRequiredTitleRule("legacy defect variance needs an entry"),
            LegacyDefectVarianceDistanceKmRangeRule(1.4, 2036.7),
        ),
    )
}
