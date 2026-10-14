package com.acme.transport.safety.scorecard.profile

import com.acme.transport.kit.text.normalize

/** A rule a scorecard profile must pass. Check returns its problems, or none. */
interface ScorecardProfileRule {
    fun check(item: ScorecardProfile): MutableList<String>
}

data class ScorecardProfileAllowedNameRule(var allowed: MutableList<String>) : ScorecardProfileRule {
    override fun check(item: ScorecardProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardProfileUpdatedAtRangeRule(var min: Int, var max: Int) : ScorecardProfileRule {
    override fun check(item: ScorecardProfile): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardProfileRequiredLabelRule(var message: String) : ScorecardProfileRule {
    override fun check(item: ScorecardProfile): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard profile. */
data class ScorecardProfileValidator(var rules: MutableList<ScorecardProfileRule>) {
    fun validate(item: ScorecardProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScorecardProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardProfileValidator(): ScorecardProfileValidator {
    return ScorecardProfileValidator(
        mutableListOf<ScorecardProfileRule>(
            ScorecardProfileAllowedNameRule(mutableListOf<String>("manual")),
            ScorecardProfileUpdatedAtRangeRule(32, 255),
            ScorecardProfileRequiredLabelRule("scorecard profile needs a value"),
        ),
    )
}
