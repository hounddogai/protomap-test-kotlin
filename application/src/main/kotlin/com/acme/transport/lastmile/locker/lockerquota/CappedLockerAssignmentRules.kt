package com.acme.transport.lastmile.locker.lockerquota

/** A rule a capped locker assignment must pass. Check returns its problems, or none. */
interface CappedLockerAssignmentRule {
    fun check(item: CappedLockerAssignment): MutableList<String>
}

data class CappedLockerAssignmentDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : CappedLockerAssignmentRule {
    override fun check(item: CappedLockerAssignment): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedLockerAssignmentRequiredLaneRule(var message: String) : CappedLockerAssignmentRule {
    override fun check(item: CappedLockerAssignment): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedLockerAssignmentCodeLengthRule(var maxLength: Int) : CappedLockerAssignmentRule {
    override fun check(item: CappedLockerAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedLockerAssignmentRequiredTitleRule(var message: String) : CappedLockerAssignmentRule {
    override fun check(item: CappedLockerAssignment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped locker assignment. */
data class CappedLockerAssignmentValidator(var rules: MutableList<CappedLockerAssignmentRule>) {
    fun validate(item: CappedLockerAssignment): MutableList<String> {
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

    fun requireValid(item: CappedLockerAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedLockerAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedLockerAssignmentValidator(): CappedLockerAssignmentValidator {
    return CappedLockerAssignmentValidator(
        mutableListOf<CappedLockerAssignmentRule>(
            CappedLockerAssignmentDurationMinutesRangeRule(16, 64),
            CappedLockerAssignmentRequiredLaneRule("capped locker assignment needs a value"),
            CappedLockerAssignmentCodeLengthRule(64),
            CappedLockerAssignmentRequiredTitleRule("capped locker assignment needs a value"),
        ),
    )
}
