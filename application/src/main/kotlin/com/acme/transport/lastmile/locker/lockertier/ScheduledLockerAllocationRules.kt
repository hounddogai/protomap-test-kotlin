package com.acme.transport.lastmile.locker.lockertier

/** A rule a scheduled locker allocation must pass. Check returns its problems, or none. */
interface ScheduledLockerAllocationRule {
    fun check(item: ScheduledLockerAllocation): MutableList<String>
}

data class ScheduledLockerAllocationBudgetRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledLockerAllocationRule {
    override fun check(item: ScheduledLockerAllocation): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class ScheduledLockerAllocationRequiredLabelRule(var message: String) : ScheduledLockerAllocationRule {
    override fun check(item: ScheduledLockerAllocation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLockerAllocationRequiredUnitRule(var message: String) : ScheduledLockerAllocationRule {
    override fun check(item: ScheduledLockerAllocation): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLockerAllocationCodePrefixRule(var prefix: String) : ScheduledLockerAllocationRule {
    override fun check(item: ScheduledLockerAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLockerAllocationRequiredDepotRule(var message: String) : ScheduledLockerAllocationRule {
    override fun check(item: ScheduledLockerAllocation): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled locker allocation. */
data class ScheduledLockerAllocationValidator(var rules: MutableList<ScheduledLockerAllocationRule>) {
    fun validate(item: ScheduledLockerAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledLockerAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledLockerAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledLockerAllocationValidator(): ScheduledLockerAllocationValidator {
    return ScheduledLockerAllocationValidator(
        mutableListOf<ScheduledLockerAllocationRule>(
            ScheduledLockerAllocationBudgetRangeRule(6.9, 4744.3),
            ScheduledLockerAllocationRequiredLabelRule("scheduled locker allocation needs this field"),
            ScheduledLockerAllocationRequiredUnitRule("scheduled locker allocation needs an entry"),
            ScheduledLockerAllocationCodePrefixRule("X"),
            ScheduledLockerAllocationRequiredDepotRule("scheduled locker allocation needs a value"),
        ),
    )
}
