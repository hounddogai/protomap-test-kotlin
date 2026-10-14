package com.acme.transport.customs.broker.adjustment

/** A rule a legacy broker plan must pass. Check returns its problems, or none. */
interface LegacyBrokerPlanRule {
    fun check(item: LegacyBrokerPlan): MutableList<String>
}

data class LegacyBrokerPlanPriorityRangeRule(var min: Int, var max: Int) : LegacyBrokerPlanRule {
    override fun check(item: LegacyBrokerPlan): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class LegacyBrokerPlanRequiredCodeRule(var message: String) : LegacyBrokerPlanRule {
    override fun check(item: LegacyBrokerPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyBrokerPlanRequiredTitleRule(var message: String) : LegacyBrokerPlanRule {
    override fun check(item: LegacyBrokerPlan): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyBrokerPlanMaximumRangeRule(var min: Double, var max: Double) : LegacyBrokerPlanRule {
    override fun check(item: LegacyBrokerPlan): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy broker plan. */
data class LegacyBrokerPlanValidator(var rules: MutableList<LegacyBrokerPlanRule>) {
    fun validate(item: LegacyBrokerPlan): MutableList<String> {
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

    fun requireValid(item: LegacyBrokerPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyBrokerPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyBrokerPlanValidator(): LegacyBrokerPlanValidator {
    return LegacyBrokerPlanValidator(
        mutableListOf<LegacyBrokerPlanRule>(
            LegacyBrokerPlanPriorityRangeRule(255, 1000),
            LegacyBrokerPlanRequiredCodeRule("legacy broker plan needs a value"),
            LegacyBrokerPlanRequiredTitleRule("legacy broker plan needs a value"),
            LegacyBrokerPlanMaximumRangeRule(4.6, 1144.7),
        ),
    )
}
