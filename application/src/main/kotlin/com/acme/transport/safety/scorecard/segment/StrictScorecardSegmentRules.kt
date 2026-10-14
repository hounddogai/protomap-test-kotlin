package com.acme.transport.safety.scorecard.segment

import com.acme.transport.kit.text.normalize

/** A rule a strict scorecard segment must pass. Check returns its problems, or none. */
interface StrictScorecardSegmentRule {
    fun check(item: StrictScorecardSegment): MutableList<String>
}

data class StrictScorecardSegmentLabelLengthRule(var maxLength: Int) : StrictScorecardSegmentRule {
    override fun check(item: StrictScorecardSegment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictScorecardSegmentRequiredLabelRule(var message: String) : StrictScorecardSegmentRule {
    override fun check(item: StrictScorecardSegment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictScorecardSegmentAllowedCodeRule(
    var allowed: MutableList<String>,
) : StrictScorecardSegmentRule {
    override fun check(item: StrictScorecardSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict scorecard segment. */
data class StrictScorecardSegmentValidator(var rules: MutableList<StrictScorecardSegmentRule>) {
    fun validate(item: StrictScorecardSegment): MutableList<String> {
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

    fun requireValid(item: StrictScorecardSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictScorecardSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictScorecardSegmentValidator(): StrictScorecardSegmentValidator {
    return StrictScorecardSegmentValidator(
        mutableListOf<StrictScorecardSegmentRule>(
            StrictScorecardSegmentLabelLengthRule(80),
            StrictScorecardSegmentRequiredLabelRule("strict scorecard segment needs a value"),
            StrictScorecardSegmentAllowedCodeRule(mutableListOf<String>("lenient", "adaptive")),
        ),
    )
}
