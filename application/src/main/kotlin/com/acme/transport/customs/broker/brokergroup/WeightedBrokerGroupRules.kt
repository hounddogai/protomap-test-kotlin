package com.acme.transport.customs.broker.brokergroup

/** A rule a weighted broker group must pass. Check returns its problems, or none. */
interface WeightedBrokerGroupRule {
    fun check(item: WeightedBrokerGroup): MutableList<String>
}

data class WeightedBrokerGroupRequiredTitleRule(var message: String) : WeightedBrokerGroupRule {
    override fun check(item: WeightedBrokerGroup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedBrokerGroupCodePrefixRule(var prefix: String) : WeightedBrokerGroupRule {
    override fun check(item: WeightedBrokerGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedBrokerGroupApprovedGuardRule(var limit: Int) : WeightedBrokerGroupRule {
    override fun check(item: WeightedBrokerGroup): MutableList<String> {
        if (item.approved && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted broker group. */
data class WeightedBrokerGroupValidator(var rules: MutableList<WeightedBrokerGroupRule>) {
    fun validate(item: WeightedBrokerGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedBrokerGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedBrokerGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedBrokerGroupValidator(): WeightedBrokerGroupValidator {
    return WeightedBrokerGroupValidator(
        mutableListOf<WeightedBrokerGroupRule>(
            WeightedBrokerGroupRequiredTitleRule("weighted broker group needs an entry"),
            WeightedBrokerGroupCodePrefixRule("ACM"),
            WeightedBrokerGroupApprovedGuardRule(1000),
        ),
    )
}
