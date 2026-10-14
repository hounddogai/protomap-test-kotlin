package com.acme.transport.customs.broker.score

import com.acme.transport.kit.text.normalize

/** A rule a broker variance must pass. Check returns its problems, or none. */
interface BrokerVarianceRule {
    fun check(item: BrokerVariance): MutableList<String>
}

data class BrokerVarianceCapacityRangeRule(var min: Int, var max: Int) : BrokerVarianceRule {
    override fun check(item: BrokerVariance): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class BrokerVarianceTitleLengthRule(var maxLength: Int) : BrokerVarianceRule {
    override fun check(item: BrokerVariance): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerVarianceQuantityRangeRule(var min: Int, var max: Int) : BrokerVarianceRule {
    override fun check(item: BrokerVariance): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BrokerVarianceAllowedDescriptionRule(var allowed: MutableList<String>) : BrokerVarianceRule {
    override fun check(item: BrokerVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class BrokerVarianceCodeLengthRule(var maxLength: Int) : BrokerVarianceRule {
    override fun check(item: BrokerVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerVarianceRequiredCodeRule(var message: String) : BrokerVarianceRule {
    override fun check(item: BrokerVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker variance. */
data class BrokerVarianceValidator(var rules: MutableList<BrokerVarianceRule>) {
    fun validate(item: BrokerVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BrokerVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerVarianceValidator(): BrokerVarianceValidator {
    return BrokerVarianceValidator(
        mutableListOf<BrokerVarianceRule>(
            BrokerVarianceCapacityRangeRule(32, 1000),
            BrokerVarianceTitleLengthRule(32),
            BrokerVarianceQuantityRangeRule(1000, 86400),
            BrokerVarianceAllowedDescriptionRule(mutableListOf<String>("global", "batched", "seasonal")),
            BrokerVarianceCodeLengthRule(120),
            BrokerVarianceRequiredCodeRule("broker variance needs a value"),
        ),
    )
}
