package com.acme.transport.customs.clearance.notice

import com.acme.transport.kit.text.normalize

/** A rule a strict clearance segment must pass. Check returns its problems, or none. */
interface StrictClearanceSegmentRule {
    fun check(item: StrictClearanceSegment): MutableList<String>
}

data class StrictClearanceSegmentOdometerRangeRule(var min: Int, var max: Int) : StrictClearanceSegmentRule {
    override fun check(item: StrictClearanceSegment): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictClearanceSegmentCodePrefixRule(var prefix: String) : StrictClearanceSegmentRule {
    override fun check(item: StrictClearanceSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictClearanceSegmentRequiredCodeRule(var message: String) : StrictClearanceSegmentRule {
    override fun check(item: StrictClearanceSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictClearanceSegmentRequiredLocaleRule(var message: String) : StrictClearanceSegmentRule {
    override fun check(item: StrictClearanceSegment): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictClearanceSegmentArchivedGuardRule(var limit: Int) : StrictClearanceSegmentRule {
    override fun check(item: StrictClearanceSegment): MutableList<String> {
        if (item.archived && item.odometer > limit) {
            return mutableListOf<String>("odometer ${item.odometer} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class StrictClearanceSegmentAllowedCodeRule(
    var allowed: MutableList<String>,
) : StrictClearanceSegmentRule {
    override fun check(item: StrictClearanceSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict clearance segment. */
data class StrictClearanceSegmentValidator(var rules: MutableList<StrictClearanceSegmentRule>) {
    fun validate(item: StrictClearanceSegment): MutableList<String> {
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

    fun requireValid(item: StrictClearanceSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictClearanceSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictClearanceSegmentValidator(): StrictClearanceSegmentValidator {
    return StrictClearanceSegmentValidator(
        mutableListOf<StrictClearanceSegmentRule>(
            StrictClearanceSegmentOdometerRangeRule(120, 255),
            StrictClearanceSegmentCodePrefixRule("X"),
            StrictClearanceSegmentRequiredCodeRule("strict clearance segment needs a value"),
            StrictClearanceSegmentRequiredLocaleRule("strict clearance segment needs an entry"),
            StrictClearanceSegmentArchivedGuardRule(1000),
            StrictClearanceSegmentAllowedCodeRule(mutableListOf<String>("fallback", "manual", "fallback")),
        ),
    )
}
