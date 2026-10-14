package com.acme.transport.customs.hold.holdsummary

/** A rule a batched hold schedule must pass. Check returns its problems, or none. */
interface BatchedHoldScheduleRule {
    fun check(item: BatchedHoldSchedule): MutableList<String>
}

data class BatchedHoldScheduleRequiredDriverIdRule(var message: String) : BatchedHoldScheduleRule {
    override fun check(item: BatchedHoldSchedule): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedHoldScheduleCodeLengthRule(var maxLength: Int) : BatchedHoldScheduleRule {
    override fun check(item: BatchedHoldSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedHoldScheduleCapacityRangeRule(var min: Int, var max: Int) : BatchedHoldScheduleRule {
    override fun check(item: BatchedHoldSchedule): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class BatchedHoldScheduleEndsAtRangeRule(var min: Int, var max: Int) : BatchedHoldScheduleRule {
    override fun check(item: BatchedHoldSchedule): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a batched hold schedule. */
data class BatchedHoldScheduleValidator(var rules: MutableList<BatchedHoldScheduleRule>) {
    fun validate(item: BatchedHoldSchedule): MutableList<String> {
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

    fun requireValid(item: BatchedHoldSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedHoldSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedHoldScheduleValidator(): BatchedHoldScheduleValidator {
    return BatchedHoldScheduleValidator(
        mutableListOf<BatchedHoldScheduleRule>(
            BatchedHoldScheduleRequiredDriverIdRule("batched hold schedule needs this field"),
            BatchedHoldScheduleCodeLengthRule(3600),
            BatchedHoldScheduleCapacityRangeRule(32, 80),
            BatchedHoldScheduleEndsAtRangeRule(16, 86400),
        ),
    )
}
