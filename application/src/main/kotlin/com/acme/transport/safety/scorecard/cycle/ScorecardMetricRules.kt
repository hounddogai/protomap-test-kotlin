package com.acme.transport.safety.scorecard.cycle

import com.acme.transport.kit.text.normalize

/** A rule a scorecard metric must pass. Check returns its problems, or none. */
interface ScorecardMetricRule {
    fun check(item: ScorecardMetric): MutableList<String>
}

data class ScorecardMetricCodeLengthRule(var maxLength: Int) : ScorecardMetricRule {
    override fun check(item: ScorecardMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardMetricRequiredTitleRule(var message: String) : ScorecardMetricRule {
    override fun check(item: ScorecardMetric): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardMetricRequiredCodeRule(var message: String) : ScorecardMetricRule {
    override fun check(item: ScorecardMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardMetricAllowedCodeRule(var allowed: MutableList<String>) : ScorecardMetricRule {
    override fun check(item: ScorecardMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard metric. */
data class ScorecardMetricValidator(var rules: MutableList<ScorecardMetricRule>) {
    fun validate(item: ScorecardMetric): MutableList<String> {
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

    fun requireValid(item: ScorecardMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardMetricValidator(): ScorecardMetricValidator {
    return ScorecardMetricValidator(
        mutableListOf<ScorecardMetricRule>(
            ScorecardMetricCodeLengthRule(120),
            ScorecardMetricRequiredTitleRule("scorecard metric needs this field"),
            ScorecardMetricRequiredCodeRule("scorecard metric needs a value"),
            ScorecardMetricAllowedCodeRule(mutableListOf<String>("capped")),
        ),
    )
}
