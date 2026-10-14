package com.acme.transport.lastmile.locker.assignment

/** A rule a locker assignment must pass. Check returns its problems, or none. */
interface LockerAssignmentRule {
    fun check(item: LockerAssignment): MutableList<String>
}

data class LockerAssignmentCategoryLengthRule(var maxLength: Int) : LockerAssignmentRule {
    override fun check(item: LockerAssignment): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerAssignmentStartsAtRangeRule(var min: Int, var max: Int) : LockerAssignmentRule {
    override fun check(item: LockerAssignment): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class LockerAssignmentSequenceRangeRule(var min: Int, var max: Int) : LockerAssignmentRule {
    override fun check(item: LockerAssignment): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LockerAssignmentCodeLengthRule(var maxLength: Int) : LockerAssignmentRule {
    override fun check(item: LockerAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerAssignmentOwnerIdLengthRule(var maxLength: Int) : LockerAssignmentRule {
    override fun check(item: LockerAssignment): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker assignment. */
data class LockerAssignmentValidator(var rules: MutableList<LockerAssignmentRule>) {
    fun validate(item: LockerAssignment): MutableList<String> {
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

    fun requireValid(item: LockerAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerAssignmentValidator(): LockerAssignmentValidator {
    return LockerAssignmentValidator(
        mutableListOf<LockerAssignmentRule>(
            LockerAssignmentCategoryLengthRule(16),
            LockerAssignmentStartsAtRangeRule(16, 1000),
            LockerAssignmentSequenceRangeRule(64, 80),
            LockerAssignmentCodeLengthRule(80),
            LockerAssignmentOwnerIdLengthRule(120),
        ),
    )
}
