package com.acme.transport.customs.broker.brokergroup

import com.acme.transport.kit.text.normalize

/** A rule a broker threshold must pass. Check returns its problems, or none. */
interface BrokerThresholdRule {
    fun check(item: BrokerThreshold): MutableList<String>
}

data class BrokerThresholdCodePrefixRule(var prefix: String) : BrokerThresholdRule {
    override fun check(item: BrokerThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BrokerThresholdCreatedAtRangeRule(var min: Int, var max: Int) : BrokerThresholdRule {
    override fun check(item: BrokerThreshold): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BrokerThresholdAllowedPhoneRule(var allowed: MutableList<String>) : BrokerThresholdRule {
    override fun check(item: BrokerThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is missing")
        }
        return mutableListOf<String>()
    }
}

data class BrokerThresholdRequiredCodeRule(var message: String) : BrokerThresholdRule {
    override fun check(item: BrokerThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker threshold. */
data class BrokerThresholdValidator(var rules: MutableList<BrokerThresholdRule>) {
    fun validate(item: BrokerThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BrokerThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerThresholdValidator(): BrokerThresholdValidator {
    return BrokerThresholdValidator(
        mutableListOf<BrokerThresholdRule>(
            BrokerThresholdCodePrefixRule("OPS"),
            BrokerThresholdCreatedAtRangeRule(16, 3600),
            BrokerThresholdAllowedPhoneRule(mutableListOf<String>("regional", "primary", "adaptive")),
            BrokerThresholdRequiredCodeRule("broker threshold needs this field"),
        ),
    )
}
