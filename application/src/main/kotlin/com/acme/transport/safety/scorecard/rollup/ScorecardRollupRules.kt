package com.acme.transport.safety.scorecard.rollup

import com.acme.transport.kit.text.normalize

/** A rule a scorecard rollup must pass. Check returns its problems, or none. */
interface ScorecardRollupRule {
    fun check(item: ScorecardRollup): MutableList<String>
}

data class ScorecardRollupRequiredLabelRule(var message: String) : ScorecardRollupRule {
    override fun check(item: ScorecardRollup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardRollupAllowedLabelRule(var allowed: MutableList<String>) : ScorecardRollupRule {
    override fun check(item: ScorecardRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardRollupPriorityRangeRule(var min: Int, var max: Int) : ScorecardRollupRule {
    override fun check(item: ScorecardRollup): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scorecard rollup. */
data class ScorecardRollupValidator(var rules: MutableList<ScorecardRollupRule>) {
    fun validate(item: ScorecardRollup): MutableList<String> {
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

    fun requireValid(item: ScorecardRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardRollupValidator(): ScorecardRollupValidator {
    return ScorecardRollupValidator(
        mutableListOf<ScorecardRollupRule>(
            ScorecardRollupRequiredLabelRule("scorecard rollup needs an entry"),
            ScorecardRollupAllowedLabelRule(mutableListOf<String>("scheduled")),
            ScorecardRollupPriorityRangeRule(120, 120),
        ),
    )
}
