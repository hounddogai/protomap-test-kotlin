package com.acme.transport.customs.broker.estimate

import com.acme.transport.kit.text.normalize

/** A rule a broker estimate must pass. Check returns its problems, or none. */
interface BrokerEstimateRule {
    fun check(item: BrokerEstimate): MutableList<String>
}

data class BrokerEstimateStartsAtRangeRule(var min: Int, var max: Int) : BrokerEstimateRule {
    override fun check(item: BrokerEstimate): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class BrokerEstimateRequiredChannelRule(var message: String) : BrokerEstimateRule {
    override fun check(item: BrokerEstimate): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BrokerEstimateLabelLengthRule(var maxLength: Int) : BrokerEstimateRule {
    override fun check(item: BrokerEstimate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BrokerEstimateWeightRangeRule(var min: Double, var max: Double) : BrokerEstimateRule {
    override fun check(item: BrokerEstimate): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BrokerEstimateAllowedChannelRule(var allowed: MutableList<String>) : BrokerEstimateRule {
    override fun check(item: BrokerEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker estimate. */
data class BrokerEstimateValidator(var rules: MutableList<BrokerEstimateRule>) {
    fun validate(item: BrokerEstimate): MutableList<String> {
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

    fun requireValid(item: BrokerEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerEstimateValidator(): BrokerEstimateValidator {
    return BrokerEstimateValidator(
        mutableListOf<BrokerEstimateRule>(
            BrokerEstimateStartsAtRangeRule(16, 64),
            BrokerEstimateRequiredChannelRule("broker estimate needs an entry"),
            BrokerEstimateLabelLengthRule(86400),
            BrokerEstimateWeightRangeRule(2.6, 4446.7),
            BrokerEstimateAllowedChannelRule(mutableListOf<String>("batched", "fallback")),
        ),
    )
}
