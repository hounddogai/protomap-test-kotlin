package com.acme.transport.emissions.idling.milestone

/** A rule a idling assignment must pass. Check returns its problems, or none. */
interface IdlingAssignmentRule {
    fun check(item: IdlingAssignment): MutableList<String>
}

data class IdlingAssignmentStopCountRangeRule(var min: Int, var max: Int) : IdlingAssignmentRule {
    override fun check(item: IdlingAssignment): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class IdlingAssignmentMinimumRangeRule(var min: Double, var max: Double) : IdlingAssignmentRule {
    override fun check(item: IdlingAssignment): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class IdlingAssignmentVisibleGuardRule(var limit: Int) : IdlingAssignmentRule {
    override fun check(item: IdlingAssignment): MutableList<String> {
        if (item.visible && item.updatedAt > limit) {
            return mutableListOf<String>("updated at ${item.updatedAt} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class IdlingAssignmentSegmentLengthRule(var maxLength: Int) : IdlingAssignmentRule {
    override fun check(item: IdlingAssignment): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingAssignmentCodePrefixRule(var prefix: String) : IdlingAssignmentRule {
    override fun check(item: IdlingAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling assignment. */
data class IdlingAssignmentValidator(var rules: MutableList<IdlingAssignmentRule>) {
    fun validate(item: IdlingAssignment): MutableList<String> {
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

    fun requireValid(item: IdlingAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingAssignmentValidator(): IdlingAssignmentValidator {
    return IdlingAssignmentValidator(
        mutableListOf<IdlingAssignmentRule>(
            IdlingAssignmentStopCountRangeRule(32, 3600),
            IdlingAssignmentMinimumRangeRule(7.2, 4588.8),
            IdlingAssignmentVisibleGuardRule(64),
            IdlingAssignmentSegmentLengthRule(16),
            IdlingAssignmentCodePrefixRule("REF"),
        ),
    )
}
