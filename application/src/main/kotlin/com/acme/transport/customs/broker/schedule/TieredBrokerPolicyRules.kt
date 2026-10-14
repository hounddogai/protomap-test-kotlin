package com.acme.transport.customs.broker.schedule

import com.acme.transport.kit.text.normalize

/** A rule a tiered broker policy must pass. Check returns its problems, or none. */
interface TieredBrokerPolicyRule {
    fun check(item: TieredBrokerPolicy): MutableList<String>
}

data class TieredBrokerPolicyBudgetRangeRule(var min: Double, var max: Double) : TieredBrokerPolicyRule {
    override fun check(item: TieredBrokerPolicy): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class TieredBrokerPolicyAllowedCodeRule(var allowed: MutableList<String>) : TieredBrokerPolicyRule {
    override fun check(item: TieredBrokerPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class TieredBrokerPolicyRequiredChecksumRule(var message: String) : TieredBrokerPolicyRule {
    override fun check(item: TieredBrokerPolicy): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredBrokerPolicyRequiredTitleRule(var message: String) : TieredBrokerPolicyRule {
    override fun check(item: TieredBrokerPolicy): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredBrokerPolicyLimitRangeRule(var min: Int, var max: Int) : TieredBrokerPolicyRule {
    override fun check(item: TieredBrokerPolicy): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered broker policy. */
data class TieredBrokerPolicyValidator(var rules: MutableList<TieredBrokerPolicyRule>) {
    fun validate(item: TieredBrokerPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredBrokerPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredBrokerPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredBrokerPolicyValidator(): TieredBrokerPolicyValidator {
    return TieredBrokerPolicyValidator(
        mutableListOf<TieredBrokerPolicyRule>(
            TieredBrokerPolicyBudgetRangeRule(9.8, 4817.1),
            TieredBrokerPolicyAllowedCodeRule(mutableListOf<String>("seasonal", "adaptive")),
            TieredBrokerPolicyRequiredChecksumRule("tiered broker policy needs an entry"),
            TieredBrokerPolicyRequiredTitleRule("tiered broker policy needs a value"),
            TieredBrokerPolicyLimitRangeRule(255, 86400),
        ),
    )
}
