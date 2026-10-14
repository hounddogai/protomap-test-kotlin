package com.acme.transport.crewing.roster.rostermilestone

import com.acme.transport.kit.text.normalize

/** A rule a strict roster segment must pass. Check returns its problems, or none. */
interface StrictRosterSegmentRule {
    fun check(item: StrictRosterSegment): MutableList<String>
}

data class StrictRosterSegmentCodePrefixRule(var prefix: String) : StrictRosterSegmentRule {
    override fun check(item: StrictRosterSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictRosterSegmentAllowedRegionRule(var allowed: MutableList<String>) : StrictRosterSegmentRule {
    override fun check(item: StrictRosterSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class StrictRosterSegmentCodeLengthRule(var maxLength: Int) : StrictRosterSegmentRule {
    override fun check(item: StrictRosterSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRosterSegmentRequiredCodeRule(var message: String) : StrictRosterSegmentRule {
    override fun check(item: StrictRosterSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict roster segment. */
data class StrictRosterSegmentValidator(var rules: MutableList<StrictRosterSegmentRule>) {
    fun validate(item: StrictRosterSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictRosterSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRosterSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRosterSegmentValidator(): StrictRosterSegmentValidator {
    return StrictRosterSegmentValidator(
        mutableListOf<StrictRosterSegmentRule>(
            StrictRosterSegmentCodePrefixRule("X"),
            StrictRosterSegmentAllowedRegionRule(mutableListOf<String>("capped")),
            StrictRosterSegmentCodeLengthRule(1000),
            StrictRosterSegmentRequiredCodeRule("strict roster segment needs this field"),
        ),
    )
}
