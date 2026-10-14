package com.acme.transport.customs.broker.budget

import com.acme.transport.kit.text.normalize

/** A rule a broker request must pass. Check returns its problems, or none. */
interface BrokerRequestRule {
    fun check(item: BrokerRequest): MutableList<String>
}

data class BrokerRequestLabelLengthRule(var maxLength: Int) : BrokerRequestRule {
    override fun check(item: BrokerRequest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerRequestFloorValueRangeRule(var min: Double, var max: Double) : BrokerRequestRule {
    override fun check(item: BrokerRequest): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BrokerRequestAllowedCodeRule(var allowed: MutableList<String>) : BrokerRequestRule {
    override fun check(item: BrokerRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker request. */
data class BrokerRequestValidator(var rules: MutableList<BrokerRequestRule>) {
    fun validate(item: BrokerRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: BrokerRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerRequestValidator(): BrokerRequestValidator {
    return BrokerRequestValidator(
        mutableListOf<BrokerRequestRule>(
            BrokerRequestLabelLengthRule(3600),
            BrokerRequestFloorValueRangeRule(2.2, 2782.6),
            BrokerRequestAllowedCodeRule(mutableListOf<String>("primary")),
        ),
    )
}
