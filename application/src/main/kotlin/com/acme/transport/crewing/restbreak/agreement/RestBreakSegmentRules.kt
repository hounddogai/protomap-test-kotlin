package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.kit.text.normalize

/** A rule a rest break segment must pass. Check returns its problems, or none. */
interface RestBreakSegmentRule {
    fun check(item: RestBreakSegment): MutableList<String>
}

data class RestBreakSegmentRequiredLabelRule(var message: String) : RestBreakSegmentRule {
    override fun check(item: RestBreakSegment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakSegmentCodeLengthRule(var maxLength: Int) : RestBreakSegmentRule {
    override fun check(item: RestBreakSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakSegmentAllowedLabelRule(var allowed: MutableList<String>) : RestBreakSegmentRule {
    override fun check(item: RestBreakSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break segment. */
data class RestBreakSegmentValidator(var rules: MutableList<RestBreakSegmentRule>) {
    fun validate(item: RestBreakSegment): MutableList<String> {
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

    fun requireValid(item: RestBreakSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakSegmentValidator(): RestBreakSegmentValidator {
    return RestBreakSegmentValidator(
        mutableListOf<RestBreakSegmentRule>(
            RestBreakSegmentRequiredLabelRule("rest break segment needs this field"),
            RestBreakSegmentCodeLengthRule(1000),
            RestBreakSegmentAllowedLabelRule(mutableListOf<String>("default")),
        ),
    )
}
