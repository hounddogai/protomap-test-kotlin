package com.acme.transport.safety.nearmiss.notice

import com.acme.transport.kit.text.normalize

/** A rule a rolling near miss segment must pass. Check returns its problems, or none. */
interface RollingNearMissSegmentRule {
    fun check(item: RollingNearMissSegment): MutableList<String>
}

data class RollingNearMissSegmentAllowedContactEmailRule(
    var allowed: MutableList<String>,
) : RollingNearMissSegmentRule {
    override fun check(item: RollingNearMissSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class RollingNearMissSegmentCodeLengthRule(var maxLength: Int) : RollingNearMissSegmentRule {
    override fun check(item: RollingNearMissSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingNearMissSegmentRequiredLabelRule(var message: String) : RollingNearMissSegmentRule {
    override fun check(item: RollingNearMissSegment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling near miss segment. */
data class RollingNearMissSegmentValidator(var rules: MutableList<RollingNearMissSegmentRule>) {
    fun validate(item: RollingNearMissSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingNearMissSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingNearMissSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingNearMissSegmentValidator(): RollingNearMissSegmentValidator {
    return RollingNearMissSegmentValidator(
        mutableListOf<RollingNearMissSegmentRule>(
            RollingNearMissSegmentAllowedContactEmailRule(mutableListOf<String>("pending", "scheduled")),
            RollingNearMissSegmentCodeLengthRule(80),
            RollingNearMissSegmentRequiredLabelRule("rolling near miss segment needs an entry"),
        ),
    )
}
