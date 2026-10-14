package com.acme.transport.lastmile.attempt.attemptdraft

/** A rule a manual attempt assignment must pass. Check returns its problems, or none. */
interface ManualAttemptAssignmentRule {
    fun check(item: ManualAttemptAssignment): MutableList<String>
}

data class ManualAttemptAssignmentBatchSizeRangeRule(var min: Int, var max: Int) : ManualAttemptAssignmentRule {
    override fun check(item: ManualAttemptAssignment): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualAttemptAssignmentRequiredTitleRule(var message: String) : ManualAttemptAssignmentRule {
    override fun check(item: ManualAttemptAssignment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualAttemptAssignmentSegmentLengthRule(var maxLength: Int) : ManualAttemptAssignmentRule {
    override fun check(item: ManualAttemptAssignment): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualAttemptAssignmentTitleLengthRule(var maxLength: Int) : ManualAttemptAssignmentRule {
    override fun check(item: ManualAttemptAssignment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualAttemptAssignmentStopCountRangeRule(var min: Int, var max: Int) : ManualAttemptAssignmentRule {
    override fun check(item: ManualAttemptAssignment): MutableList<String> {
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

/** Runs every rule of a manual attempt assignment. */
data class ManualAttemptAssignmentValidator(var rules: MutableList<ManualAttemptAssignmentRule>) {
    fun validate(item: ManualAttemptAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualAttemptAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualAttemptAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultManualAttemptAssignmentValidator(): ManualAttemptAssignmentValidator {
    return ManualAttemptAssignmentValidator(
        mutableListOf<ManualAttemptAssignmentRule>(
            ManualAttemptAssignmentBatchSizeRangeRule(32, 255),
            ManualAttemptAssignmentRequiredTitleRule("manual attempt assignment needs a value"),
            ManualAttemptAssignmentSegmentLengthRule(86400),
            ManualAttemptAssignmentTitleLengthRule(64),
            ManualAttemptAssignmentStopCountRangeRule(16, 120),
        ),
    )
}
