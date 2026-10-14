package com.acme.transport.freightpay.dispute.policy

/** A rule a dispute plan must pass. Check returns its problems, or none. */
interface DisputePlanRule {
    fun check(item: DisputePlan): MutableList<String>
}

data class DisputePlanRequiredNameRule(var message: String) : DisputePlanRule {
    override fun check(item: DisputePlan): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputePlanCodePrefixRule(var prefix: String) : DisputePlanRule {
    override fun check(item: DisputePlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DisputePlanCostRangeRule(var min: Double, var max: Double) : DisputePlanRule {
    override fun check(item: DisputePlan): MutableList<String> {
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

data class DisputePlanLabelLengthRule(var maxLength: Int) : DisputePlanRule {
    override fun check(item: DisputePlan): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute plan. */
data class DisputePlanValidator(var rules: MutableList<DisputePlanRule>) {
    fun validate(item: DisputePlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DisputePlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputePlan>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputePlanValidator(): DisputePlanValidator {
    return DisputePlanValidator(
        mutableListOf<DisputePlanRule>(
            DisputePlanRequiredNameRule("dispute plan needs a value"),
            DisputePlanCodePrefixRule("X"),
            DisputePlanCostRangeRule(7.0, 261.2),
            DisputePlanLabelLengthRule(120),
        ),
    )
}
