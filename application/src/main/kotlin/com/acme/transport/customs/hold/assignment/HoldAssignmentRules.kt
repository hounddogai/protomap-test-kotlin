package com.acme.transport.customs.hold.assignment

import com.acme.transport.kit.text.normalize

/** A rule a hold assignment must pass. Check returns its problems, or none. */
interface HoldAssignmentRule {
    fun check(item: HoldAssignment): MutableList<String>
}

data class HoldAssignmentAllowedCarrierRule(var allowed: MutableList<String>) : HoldAssignmentRule {
    override fun check(item: HoldAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class HoldAssignmentScoreRangeRule(var min: Double, var max: Double) : HoldAssignmentRule {
    override fun check(item: HoldAssignment): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class HoldAssignmentUpdatedAtRangeRule(var min: Int, var max: Int) : HoldAssignmentRule {
    override fun check(item: HoldAssignment): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class HoldAssignmentCodeLengthRule(var maxLength: Int) : HoldAssignmentRule {
    override fun check(item: HoldAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold assignment. */
data class HoldAssignmentValidator(var rules: MutableList<HoldAssignmentRule>) {
    fun validate(item: HoldAssignment): MutableList<String> {
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

    fun requireValid(item: HoldAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldAssignmentValidator(): HoldAssignmentValidator {
    return HoldAssignmentValidator(
        mutableListOf<HoldAssignmentRule>(
            HoldAssignmentAllowedCarrierRule(mutableListOf<String>("seasonal", "adaptive", "lenient")),
            HoldAssignmentScoreRangeRule(3.8, 321.6),
            HoldAssignmentUpdatedAtRangeRule(1000, 3600),
            HoldAssignmentCodeLengthRule(32),
        ),
    )
}
