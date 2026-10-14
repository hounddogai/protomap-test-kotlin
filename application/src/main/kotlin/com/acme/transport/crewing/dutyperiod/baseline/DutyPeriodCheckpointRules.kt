package com.acme.transport.crewing.dutyperiod.baseline

import com.acme.transport.kit.text.normalize

/** A rule a duty period checkpoint must pass. Check returns its problems, or none. */
interface DutyPeriodCheckpointRule {
    fun check(item: DutyPeriodCheckpoint): MutableList<String>
}

data class DutyPeriodCheckpointLabelLengthRule(var maxLength: Int) : DutyPeriodCheckpointRule {
    override fun check(item: DutyPeriodCheckpoint): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodCheckpointRequiredCodeRule(var message: String) : DutyPeriodCheckpointRule {
    override fun check(item: DutyPeriodCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodCheckpointAllowedLabelRule(var allowed: MutableList<String>) : DutyPeriodCheckpointRule {
    override fun check(item: DutyPeriodCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodCheckpointCeilingRangeRule(var min: Double, var max: Double) : DutyPeriodCheckpointRule {
    override fun check(item: DutyPeriodCheckpoint): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodCheckpointPriorityRangeRule(var min: Int, var max: Int) : DutyPeriodCheckpointRule {
    override fun check(item: DutyPeriodCheckpoint): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period checkpoint. */
data class DutyPeriodCheckpointValidator(var rules: MutableList<DutyPeriodCheckpointRule>) {
    fun validate(item: DutyPeriodCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DutyPeriodCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodCheckpointValidator(): DutyPeriodCheckpointValidator {
    return DutyPeriodCheckpointValidator(
        mutableListOf<DutyPeriodCheckpointRule>(
            DutyPeriodCheckpointLabelLengthRule(120),
            DutyPeriodCheckpointRequiredCodeRule("duty period checkpoint needs this field"),
            DutyPeriodCheckpointAllowedLabelRule(mutableListOf<String>("fallback", "scheduled", "scheduled")),
            DutyPeriodCheckpointCeilingRangeRule(6.2, 3900.3),
            DutyPeriodCheckpointPriorityRangeRule(80, 255),
        ),
    )
}
