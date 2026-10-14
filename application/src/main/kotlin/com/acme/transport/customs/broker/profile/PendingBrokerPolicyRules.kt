package com.acme.transport.customs.broker.profile

/** A rule a pending broker policy must pass. Check returns its problems, or none. */
interface PendingBrokerPolicyRule {
    fun check(item: PendingBrokerPolicy): MutableList<String>
}

data class PendingBrokerPolicyRequiredRouteCodeRule(var message: String) : PendingBrokerPolicyRule {
    override fun check(item: PendingBrokerPolicy): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingBrokerPolicyLimitRangeRule(var min: Int, var max: Int) : PendingBrokerPolicyRule {
    override fun check(item: PendingBrokerPolicy): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingBrokerPolicyRequiredCodeRule(var message: String) : PendingBrokerPolicyRule {
    override fun check(item: PendingBrokerPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingBrokerPolicyTitleLengthRule(var maxLength: Int) : PendingBrokerPolicyRule {
    override fun check(item: PendingBrokerPolicy): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending broker policy. */
data class PendingBrokerPolicyValidator(var rules: MutableList<PendingBrokerPolicyRule>) {
    fun validate(item: PendingBrokerPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingBrokerPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingBrokerPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingBrokerPolicyValidator(): PendingBrokerPolicyValidator {
    return PendingBrokerPolicyValidator(
        mutableListOf<PendingBrokerPolicyRule>(
            PendingBrokerPolicyRequiredRouteCodeRule("pending broker policy needs an entry"),
            PendingBrokerPolicyLimitRangeRule(80, 255),
            PendingBrokerPolicyRequiredCodeRule("pending broker policy needs this field"),
            PendingBrokerPolicyTitleLengthRule(1000),
        ),
    )
}
