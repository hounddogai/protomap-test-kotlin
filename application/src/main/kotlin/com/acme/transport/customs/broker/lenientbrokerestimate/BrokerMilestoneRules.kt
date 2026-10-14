package com.acme.transport.customs.broker.lenientbrokerestimate

/** A rule a broker milestone must pass. Check returns its problems, or none. */
interface BrokerMilestoneRule {
    fun check(item: BrokerMilestone): MutableList<String>
}

data class BrokerMilestoneRequiredOwnerIdRule(var message: String) : BrokerMilestoneRule {
    override fun check(item: BrokerMilestone): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerMilestoneWeightRangeRule(var min: Double, var max: Double) : BrokerMilestoneRule {
    override fun check(item: BrokerMilestone): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class BrokerMilestoneRequiredNameRule(var message: String) : BrokerMilestoneRule {
    override fun check(item: BrokerMilestone): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerMilestoneCodePrefixRule(var prefix: String) : BrokerMilestoneRule {
    override fun check(item: BrokerMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BrokerMilestoneNameLengthRule(var maxLength: Int) : BrokerMilestoneRule {
    override fun check(item: BrokerMilestone): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker milestone. */
data class BrokerMilestoneValidator(var rules: MutableList<BrokerMilestoneRule>) {
    fun validate(item: BrokerMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BrokerMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerMilestoneValidator(): BrokerMilestoneValidator {
    return BrokerMilestoneValidator(
        mutableListOf<BrokerMilestoneRule>(
            BrokerMilestoneRequiredOwnerIdRule("broker milestone needs an entry"),
            BrokerMilestoneWeightRangeRule(2.3, 3732.4),
            BrokerMilestoneRequiredNameRule("broker milestone needs this field"),
            BrokerMilestoneCodePrefixRule("X"),
            BrokerMilestoneNameLengthRule(32),
        ),
    )
}
