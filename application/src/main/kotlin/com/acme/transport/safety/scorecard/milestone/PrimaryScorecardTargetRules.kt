package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.kit.text.normalize

/** A rule a primary scorecard target must pass. Check returns its problems, or none. */
interface PrimaryScorecardTargetRule {
    fun check(item: PrimaryScorecardTarget): MutableList<String>
}

data class PrimaryScorecardTargetRequiredCodeRule(var message: String) : PrimaryScorecardTargetRule {
    override fun check(item: PrimaryScorecardTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryScorecardTargetAllowedCodeRule(
    var allowed: MutableList<String>,
) : PrimaryScorecardTargetRule {
    override fun check(item: PrimaryScorecardTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryScorecardTargetScoreRangeRule(var min: Double, var max: Double) : PrimaryScorecardTargetRule {
    override fun check(item: PrimaryScorecardTarget): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryScorecardTargetLabelLengthRule(var maxLength: Int) : PrimaryScorecardTargetRule {
    override fun check(item: PrimaryScorecardTarget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary scorecard target. */
data class PrimaryScorecardTargetValidator(var rules: MutableList<PrimaryScorecardTargetRule>) {
    fun validate(item: PrimaryScorecardTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryScorecardTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryScorecardTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryScorecardTargetValidator(): PrimaryScorecardTargetValidator {
    return PrimaryScorecardTargetValidator(
        mutableListOf<PrimaryScorecardTargetRule>(
            PrimaryScorecardTargetRequiredCodeRule("primary scorecard target needs a value"),
            PrimaryScorecardTargetAllowedCodeRule(mutableListOf<String>("scheduled", "scheduled")),
            PrimaryScorecardTargetScoreRangeRule(0.9, 69.1),
            PrimaryScorecardTargetLabelLengthRule(120),
        ),
    )
}
