package com.acme.transport.safety.scorecard.window

import com.acme.transport.kit.text.normalize

/** A rule a scorecard batch must pass. Check returns its problems, or none. */
interface ScorecardBatchRule {
    fun check(item: ScorecardBatch): MutableList<String>
}

data class ScorecardBatchRequiredLabelRule(var message: String) : ScorecardBatchRule {
    override fun check(item: ScorecardBatch): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardBatchEndsAtRangeRule(var min: Int, var max: Int) : ScorecardBatchRule {
    override fun check(item: ScorecardBatch): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class ScorecardBatchDepotLengthRule(var maxLength: Int) : ScorecardBatchRule {
    override fun check(item: ScorecardBatch): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardBatchAllowedLabelRule(var allowed: MutableList<String>) : ScorecardBatchRule {
    override fun check(item: ScorecardBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is too long")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardBatchLoadKgRangeRule(var min: Double, var max: Double) : ScorecardBatchRule {
    override fun check(item: ScorecardBatch): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scorecard batch. */
data class ScorecardBatchValidator(var rules: MutableList<ScorecardBatchRule>) {
    fun validate(item: ScorecardBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScorecardBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardBatchValidator(): ScorecardBatchValidator {
    return ScorecardBatchValidator(
        mutableListOf<ScorecardBatchRule>(
            ScorecardBatchRequiredLabelRule("scorecard batch needs this field"),
            ScorecardBatchEndsAtRangeRule(120, 1000),
            ScorecardBatchDepotLengthRule(86400),
            ScorecardBatchAllowedLabelRule(mutableListOf<String>("lenient")),
            ScorecardBatchLoadKgRangeRule(9.1, 4426.6),
        ),
    )
}
