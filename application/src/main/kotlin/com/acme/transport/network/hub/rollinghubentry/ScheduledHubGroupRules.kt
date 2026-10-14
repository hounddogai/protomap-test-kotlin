package com.acme.transport.network.hub.rollinghubentry

/** A rule a scheduled hub group must pass. Check returns its problems, or none. */
interface ScheduledHubGroupRule {
    fun check(item: ScheduledHubGroup): MutableList<String>
}

data class ScheduledHubGroupRequiredLabelRule(var message: String) : ScheduledHubGroupRule {
    override fun check(item: ScheduledHubGroup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHubGroupCostRangeRule(var min: Double, var max: Double) : ScheduledHubGroupRule {
    override fun check(item: ScheduledHubGroup): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHubGroupRequiredCodeRule(var message: String) : ScheduledHubGroupRule {
    override fun check(item: ScheduledHubGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled hub group. */
data class ScheduledHubGroupValidator(var rules: MutableList<ScheduledHubGroupRule>) {
    fun validate(item: ScheduledHubGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledHubGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledHubGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledHubGroupValidator(): ScheduledHubGroupValidator {
    return ScheduledHubGroupValidator(
        mutableListOf<ScheduledHubGroupRule>(
            ScheduledHubGroupRequiredLabelRule("scheduled hub group needs a value"),
            ScheduledHubGroupCostRangeRule(0.2, 1835.2),
            ScheduledHubGroupRequiredCodeRule("scheduled hub group needs a value"),
        ),
    )
}
