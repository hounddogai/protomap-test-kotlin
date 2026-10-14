package com.acme.transport.fleetops.tire.plan

/** A rule a pending tire schedule must pass. Check returns its problems, or none. */
interface PendingTireScheduleRule {
    fun check(item: PendingTireSchedule): MutableList<String>
}

data class PendingTireScheduleCodePrefixRule(var prefix: String) : PendingTireScheduleRule {
    override fun check(item: PendingTireSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingTireScheduleRequiredOwnerIdRule(var message: String) : PendingTireScheduleRule {
    override fun check(item: PendingTireSchedule): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingTireScheduleCostRangeRule(var min: Double, var max: Double) : PendingTireScheduleRule {
    override fun check(item: PendingTireSchedule): MutableList<String> {
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

/** Runs every rule of a pending tire schedule. */
data class PendingTireScheduleValidator(var rules: MutableList<PendingTireScheduleRule>) {
    fun validate(item: PendingTireSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingTireSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingTireSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingTireScheduleValidator(): PendingTireScheduleValidator {
    return PendingTireScheduleValidator(
        mutableListOf<PendingTireScheduleRule>(
            PendingTireScheduleCodePrefixRule("Q"),
            PendingTireScheduleRequiredOwnerIdRule("pending tire schedule needs an entry"),
            PendingTireScheduleCostRangeRule(5.7, 3448.5),
        ),
    )
}
