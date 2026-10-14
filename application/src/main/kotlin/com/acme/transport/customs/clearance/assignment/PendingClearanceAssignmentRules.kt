package com.acme.transport.customs.clearance.assignment

/** A rule a pending clearance assignment must pass. Check returns its problems, or none. */
interface PendingClearanceAssignmentRule {
    fun check(item: PendingClearanceAssignment): MutableList<String>
}

data class PendingClearanceAssignmentCodePrefixRule(var prefix: String) : PendingClearanceAssignmentRule {
    override fun check(item: PendingClearanceAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingClearanceAssignmentDescriptionLengthRule(
    var maxLength: Int,
) : PendingClearanceAssignmentRule {
    override fun check(item: PendingClearanceAssignment): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingClearanceAssignmentScoreRangeRule(
    var min: Double,
    var max: Double,
) : PendingClearanceAssignmentRule {
    override fun check(item: PendingClearanceAssignment): MutableList<String> {
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

data class PendingClearanceAssignmentRevisionRangeRule(
    var min: Int,
    var max: Int,
) : PendingClearanceAssignmentRule {
    override fun check(item: PendingClearanceAssignment): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingClearanceAssignmentShiftCodeLengthRule(var maxLength: Int) : PendingClearanceAssignmentRule {
    override fun check(item: PendingClearanceAssignment): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending clearance assignment. */
data class PendingClearanceAssignmentValidator(var rules: MutableList<PendingClearanceAssignmentRule>) {
    fun validate(item: PendingClearanceAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingClearanceAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingClearanceAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingClearanceAssignmentValidator(): PendingClearanceAssignmentValidator {
    return PendingClearanceAssignmentValidator(
        mutableListOf<PendingClearanceAssignmentRule>(
            PendingClearanceAssignmentCodePrefixRule("ACM"),
            PendingClearanceAssignmentDescriptionLengthRule(1000),
            PendingClearanceAssignmentScoreRangeRule(3.9, 292.2),
            PendingClearanceAssignmentRevisionRangeRule(120, 86400),
            PendingClearanceAssignmentShiftCodeLengthRule(16),
        ),
    )
}
