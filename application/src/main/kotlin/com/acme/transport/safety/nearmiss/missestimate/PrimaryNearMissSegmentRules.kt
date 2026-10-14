package com.acme.transport.safety.nearmiss.missestimate

import com.acme.transport.kit.text.normalize

/** A rule a primary near miss segment must pass. Check returns its problems, or none. */
interface PrimaryNearMissSegmentRule {
    fun check(item: PrimaryNearMissSegment): MutableList<String>
}

data class PrimaryNearMissSegmentTitleLengthRule(var maxLength: Int) : PrimaryNearMissSegmentRule {
    override fun check(item: PrimaryNearMissSegment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNearMissSegmentCodePrefixRule(var prefix: String) : PrimaryNearMissSegmentRule {
    override fun check(item: PrimaryNearMissSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNearMissSegmentRequiredCodeRule(var message: String) : PrimaryNearMissSegmentRule {
    override fun check(item: PrimaryNearMissSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNearMissSegmentUnitLengthRule(var maxLength: Int) : PrimaryNearMissSegmentRule {
    override fun check(item: PrimaryNearMissSegment): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNearMissSegmentAllowedUnitRule(
    var allowed: MutableList<String>,
) : PrimaryNearMissSegmentRule {
    override fun check(item: PrimaryNearMissSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary near miss segment. */
data class PrimaryNearMissSegmentValidator(var rules: MutableList<PrimaryNearMissSegmentRule>) {
    fun validate(item: PrimaryNearMissSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryNearMissSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryNearMissSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryNearMissSegmentValidator(): PrimaryNearMissSegmentValidator {
    return PrimaryNearMissSegmentValidator(
        mutableListOf<PrimaryNearMissSegmentRule>(
            PrimaryNearMissSegmentTitleLengthRule(16),
            PrimaryNearMissSegmentCodePrefixRule("Q"),
            PrimaryNearMissSegmentRequiredCodeRule("primary near miss segment needs an entry"),
            PrimaryNearMissSegmentUnitLengthRule(64),
            PrimaryNearMissSegmentAllowedUnitRule(mutableListOf<String>("strict", "adaptive")),
        ),
    )
}
