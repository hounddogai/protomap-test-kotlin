package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.kit.text.normalize

/** A rule a weighted scorecard profile must pass. Check returns its problems, or none. */
interface WeightedScorecardProfileRule {
    fun check(item: WeightedScorecardProfile): MutableList<String>
}

data class WeightedScorecardProfileUnitLengthRule(var maxLength: Int) : WeightedScorecardProfileRule {
    override fun check(item: WeightedScorecardProfile): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedScorecardProfileRequiredLabelRule(var message: String) : WeightedScorecardProfileRule {
    override fun check(item: WeightedScorecardProfile): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedScorecardProfileAllowedRegionRule(
    var allowed: MutableList<String>,
) : WeightedScorecardProfileRule {
    override fun check(item: WeightedScorecardProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class WeightedScorecardProfileCodeLengthRule(var maxLength: Int) : WeightedScorecardProfileRule {
    override fun check(item: WeightedScorecardProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedScorecardProfileRegionLengthRule(var maxLength: Int) : WeightedScorecardProfileRule {
    override fun check(item: WeightedScorecardProfile): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted scorecard profile. */
data class WeightedScorecardProfileValidator(var rules: MutableList<WeightedScorecardProfileRule>) {
    fun validate(item: WeightedScorecardProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedScorecardProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedScorecardProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedScorecardProfileValidator(): WeightedScorecardProfileValidator {
    return WeightedScorecardProfileValidator(
        mutableListOf<WeightedScorecardProfileRule>(
            WeightedScorecardProfileUnitLengthRule(16),
            WeightedScorecardProfileRequiredLabelRule("weighted scorecard profile needs a value"),
            WeightedScorecardProfileAllowedRegionRule(mutableListOf<String>()),
            WeightedScorecardProfileCodeLengthRule(16),
            WeightedScorecardProfileRegionLengthRule(16),
        ),
    )
}
