package com.acme.transport.linehaul.relay.target

/** A rule a relay plan must pass. Check returns its problems, or none. */
interface RelayPlanRule {
    fun check(item: RelayPlan): MutableList<String>
}

data class RelayPlanDiscountRangeRule(var min: Double, var max: Double) : RelayPlanRule {
    override fun check(item: RelayPlan): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class RelayPlanApprovedGuardRule(var limit: Double) : RelayPlanRule {
    override fun check(item: RelayPlan): MutableList<String> {
        if (item.approved && item.floorValue > limit) {
            return mutableListOf<String>("floor value ${item.floorValue} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class RelayPlanLimitRangeRule(var min: Int, var max: Int) : RelayPlanRule {
    override fun check(item: RelayPlan): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a relay plan. */
data class RelayPlanValidator(var rules: MutableList<RelayPlanRule>) {
    fun validate(item: RelayPlan): MutableList<String> {
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

    fun requireValid(item: RelayPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayPlanValidator(): RelayPlanValidator {
    return RelayPlanValidator(
        mutableListOf<RelayPlanRule>(
            RelayPlanDiscountRangeRule(5.0, 3017.7),
            RelayPlanApprovedGuardRule(1.7),
            RelayPlanLimitRangeRule(32, 32),
        ),
    )
}
