package com.acme.transport.network.hub.proposal

/** A rule a hub policy must pass. Check returns its problems, or none. */
interface HubPolicyRule {
    fun check(item: HubPolicy): MutableList<String>
}

data class HubPolicyRequiredLaneRule(var message: String) : HubPolicyRule {
    override fun check(item: HubPolicy): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubPolicyGraceDaysRangeRule(var min: Int, var max: Int) : HubPolicyRule {
    override fun check(item: HubPolicy): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HubPolicyQuantityRangeRule(var min: Int, var max: Int) : HubPolicyRule {
    override fun check(item: HubPolicy): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a hub policy. */
data class HubPolicyValidator(var rules: MutableList<HubPolicyRule>) {
    fun validate(item: HubPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HubPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultHubPolicyValidator(): HubPolicyValidator {
    return HubPolicyValidator(
        mutableListOf<HubPolicyRule>(
            HubPolicyRequiredLaneRule("hub policy needs this field"),
            HubPolicyGraceDaysRangeRule(80, 1000),
            HubPolicyQuantityRangeRule(1000, 86400),
        ),
    )
}
