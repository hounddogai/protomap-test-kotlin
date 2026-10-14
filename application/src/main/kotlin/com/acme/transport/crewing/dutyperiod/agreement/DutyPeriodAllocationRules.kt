package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.kit.text.normalize

/** A rule a duty period allocation must pass. Check returns its problems, or none. */
interface DutyPeriodAllocationRule {
    fun check(item: DutyPeriodAllocation): MutableList<String>
}

data class DutyPeriodAllocationStartsAtRangeRule(var min: Int, var max: Int) : DutyPeriodAllocationRule {
    override fun check(item: DutyPeriodAllocation): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodAllocationAllowedLabelRule(var allowed: MutableList<String>) : DutyPeriodAllocationRule {
    override fun check(item: DutyPeriodAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodAllocationCodePrefixRule(var prefix: String) : DutyPeriodAllocationRule {
    override fun check(item: DutyPeriodAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodAllocationCapacityRangeRule(var min: Int, var max: Int) : DutyPeriodAllocationRule {
    override fun check(item: DutyPeriodAllocation): MutableList<String> {
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

/** Runs every rule of a duty period allocation. */
data class DutyPeriodAllocationValidator(var rules: MutableList<DutyPeriodAllocationRule>) {
    fun validate(item: DutyPeriodAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DutyPeriodAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodAllocationValidator(): DutyPeriodAllocationValidator {
    return DutyPeriodAllocationValidator(
        mutableListOf<DutyPeriodAllocationRule>(
            DutyPeriodAllocationStartsAtRangeRule(255, 255),
            DutyPeriodAllocationAllowedLabelRule(mutableListOf<String>("fallback", "regional", "manual")),
            DutyPeriodAllocationCodePrefixRule("REF"),
            DutyPeriodAllocationCapacityRangeRule(64, 3600),
        ),
    )
}
