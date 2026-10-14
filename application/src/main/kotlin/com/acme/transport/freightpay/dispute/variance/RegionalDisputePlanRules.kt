package com.acme.transport.freightpay.dispute.variance

/** A rule a regional dispute plan must pass. Check returns its problems, or none. */
interface RegionalDisputePlanRule {
    fun check(item: RegionalDisputePlan): MutableList<String>
}

data class RegionalDisputePlanRequiredLabelRule(var message: String) : RegionalDisputePlanRule {
    override fun check(item: RegionalDisputePlan): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDisputePlanLabelLengthRule(var maxLength: Int) : RegionalDisputePlanRule {
    override fun check(item: RegionalDisputePlan): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDisputePlanLimitRangeRule(var min: Int, var max: Int) : RegionalDisputePlanRule {
    override fun check(item: RegionalDisputePlan): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDisputePlanRequiredCodeRule(var message: String) : RegionalDisputePlanRule {
    override fun check(item: RegionalDisputePlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional dispute plan. */
data class RegionalDisputePlanValidator(var rules: MutableList<RegionalDisputePlanRule>) {
    fun validate(item: RegionalDisputePlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalDisputePlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDisputePlan>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDisputePlanValidator(): RegionalDisputePlanValidator {
    return RegionalDisputePlanValidator(
        mutableListOf<RegionalDisputePlanRule>(
            RegionalDisputePlanRequiredLabelRule("regional dispute plan needs an entry"),
            RegionalDisputePlanLabelLengthRule(16),
            RegionalDisputePlanLimitRangeRule(16, 255),
            RegionalDisputePlanRequiredCodeRule("regional dispute plan needs an entry"),
        ),
    )
}
