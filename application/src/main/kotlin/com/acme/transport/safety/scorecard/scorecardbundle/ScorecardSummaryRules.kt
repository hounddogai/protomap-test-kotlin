package com.acme.transport.safety.scorecard.scorecardbundle

import com.acme.transport.kit.text.normalize

/** A rule a scorecard summary must pass. Check returns its problems, or none. */
interface ScorecardSummaryRule {
    fun check(item: ScorecardSummary): MutableList<String>
}

data class ScorecardSummaryWindowDaysRangeRule(var min: Int, var max: Int) : ScorecardSummaryRule {
    override fun check(item: ScorecardSummary): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardSummaryDistanceKmRangeRule(var min: Double, var max: Double) : ScorecardSummaryRule {
    override fun check(item: ScorecardSummary): MutableList<String> {
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

data class ScorecardSummaryRequiredNameRule(var message: String) : ScorecardSummaryRule {
    override fun check(item: ScorecardSummary): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardSummaryNameLengthRule(var maxLength: Int) : ScorecardSummaryRule {
    override fun check(item: ScorecardSummary): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardSummaryAllowedCodeRule(var allowed: MutableList<String>) : ScorecardSummaryRule {
    override fun check(item: ScorecardSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard summary. */
data class ScorecardSummaryValidator(var rules: MutableList<ScorecardSummaryRule>) {
    fun validate(item: ScorecardSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScorecardSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardSummaryValidator(): ScorecardSummaryValidator {
    return ScorecardSummaryValidator(
        mutableListOf<ScorecardSummaryRule>(
            ScorecardSummaryWindowDaysRangeRule(120, 255),
            ScorecardSummaryDistanceKmRangeRule(7.8, 407.1),
            ScorecardSummaryRequiredNameRule("scorecard summary needs an entry"),
            ScorecardSummaryNameLengthRule(64),
            ScorecardSummaryAllowedCodeRule(mutableListOf<String>("scheduled")),
        ),
    )
}
