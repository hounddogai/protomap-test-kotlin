package com.acme.transport.crewing.dutyperiod.assignment

import com.acme.transport.kit.text.normalize

/** A rule a duty period assignment must pass. Check returns its problems, or none. */
interface DutyPeriodAssignmentRule {
    fun check(item: DutyPeriodAssignment): MutableList<String>
}

data class DutyPeriodAssignmentBatchSizeRangeRule(var min: Int, var max: Int) : DutyPeriodAssignmentRule {
    override fun check(item: DutyPeriodAssignment): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodAssignmentRequiredCodeRule(var message: String) : DutyPeriodAssignmentRule {
    override fun check(item: DutyPeriodAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodAssignmentAllowedCodeRule(var allowed: MutableList<String>) : DutyPeriodAssignmentRule {
    override fun check(item: DutyPeriodAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period assignment. */
data class DutyPeriodAssignmentValidator(var rules: MutableList<DutyPeriodAssignmentRule>) {
    fun validate(item: DutyPeriodAssignment): MutableList<String> {
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

    fun requireValid(item: DutyPeriodAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodAssignmentValidator(): DutyPeriodAssignmentValidator {
    return DutyPeriodAssignmentValidator(
        mutableListOf<DutyPeriodAssignmentRule>(
            DutyPeriodAssignmentBatchSizeRangeRule(16, 64),
            DutyPeriodAssignmentRequiredCodeRule("duty period assignment needs a value"),
            DutyPeriodAssignmentAllowedCodeRule(mutableListOf<String>("tiered", "strict", "batched")),
        ),
    )
}
