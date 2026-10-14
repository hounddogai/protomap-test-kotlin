package com.acme.transport.lastmile.attempt.batch

/** A rule a scheduled attempt slot must pass. Check returns its problems, or none. */
interface ScheduledAttemptSlotRule {
    fun check(item: ScheduledAttemptSlot): MutableList<String>
}

data class ScheduledAttemptSlotCodeLengthRule(var maxLength: Int) : ScheduledAttemptSlotRule {
    override fun check(item: ScheduledAttemptSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAttemptSlotLaneLengthRule(var maxLength: Int) : ScheduledAttemptSlotRule {
    override fun check(item: ScheduledAttemptSlot): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAttemptSlotRequiredDescriptionRule(var message: String) : ScheduledAttemptSlotRule {
    override fun check(item: ScheduledAttemptSlot): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAttemptSlotCostRangeRule(var min: Double, var max: Double) : ScheduledAttemptSlotRule {
    override fun check(item: ScheduledAttemptSlot): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scheduled attempt slot. */
data class ScheduledAttemptSlotValidator(var rules: MutableList<ScheduledAttemptSlotRule>) {
    fun validate(item: ScheduledAttemptSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledAttemptSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledAttemptSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledAttemptSlotValidator(): ScheduledAttemptSlotValidator {
    return ScheduledAttemptSlotValidator(
        mutableListOf<ScheduledAttemptSlotRule>(
            ScheduledAttemptSlotCodeLengthRule(1000),
            ScheduledAttemptSlotLaneLengthRule(80),
            ScheduledAttemptSlotRequiredDescriptionRule("scheduled attempt slot needs a value"),
            ScheduledAttemptSlotCostRangeRule(8.3, 3328.6),
        ),
    )
}
