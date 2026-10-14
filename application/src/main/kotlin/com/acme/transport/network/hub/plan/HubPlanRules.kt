package com.acme.transport.network.hub.plan

/** A rule a hub plan must pass. Check returns its problems, or none. */
interface HubPlanRule {
    fun check(item: HubPlan): MutableList<String>
}

data class HubPlanRequiredPhoneRule(var message: String) : HubPlanRule {
    override fun check(item: HubPlan): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubPlanNameLengthRule(var maxLength: Int) : HubPlanRule {
    override fun check(item: HubPlan): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubPlanScoreRangeRule(var min: Double, var max: Double) : HubPlanRule {
    override fun check(item: HubPlan): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HubPlanRequiredCodeRule(var message: String) : HubPlanRule {
    override fun check(item: HubPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubPlanRequiredNameRule(var message: String) : HubPlanRule {
    override fun check(item: HubPlan): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub plan. */
data class HubPlanValidator(var rules: MutableList<HubPlanRule>) {
    fun validate(item: HubPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HubPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultHubPlanValidator(): HubPlanValidator {
    return HubPlanValidator(
        mutableListOf<HubPlanRule>(
            HubPlanRequiredPhoneRule("hub plan needs a value"),
            HubPlanNameLengthRule(1000),
            HubPlanScoreRangeRule(4.9, 4640.2),
            HubPlanRequiredCodeRule("hub plan needs this field"),
            HubPlanRequiredNameRule("hub plan needs this field"),
        ),
    )
}
