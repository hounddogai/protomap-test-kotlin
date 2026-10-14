package com.acme.transport.customs.broker.forecast

/** A rule a strict broker allocation must pass. Check returns its problems, or none. */
interface StrictBrokerAllocationRule {
    fun check(item: StrictBrokerAllocation): MutableList<String>
}

data class StrictBrokerAllocationAmountRangeRule(
    var min: Double,
    var max: Double,
) : StrictBrokerAllocationRule {
    override fun check(item: StrictBrokerAllocation): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictBrokerAllocationCodePrefixRule(var prefix: String) : StrictBrokerAllocationRule {
    override fun check(item: StrictBrokerAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictBrokerAllocationMaximumRangeRule(
    var min: Double,
    var max: Double,
) : StrictBrokerAllocationRule {
    override fun check(item: StrictBrokerAllocation): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class StrictBrokerAllocationPinnedGuardRule(var limit: Int) : StrictBrokerAllocationRule {
    override fun check(item: StrictBrokerAllocation): MutableList<String> {
        if (item.pinned && item.startsAt > limit) {
            return mutableListOf<String>("starts at ${item.startsAt} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class StrictBrokerAllocationCodeLengthRule(var maxLength: Int) : StrictBrokerAllocationRule {
    override fun check(item: StrictBrokerAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictBrokerAllocationRequiredTitleRule(var message: String) : StrictBrokerAllocationRule {
    override fun check(item: StrictBrokerAllocation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict broker allocation. */
data class StrictBrokerAllocationValidator(var rules: MutableList<StrictBrokerAllocationRule>) {
    fun validate(item: StrictBrokerAllocation): MutableList<String> {
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

    fun requireValid(item: StrictBrokerAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictBrokerAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictBrokerAllocationValidator(): StrictBrokerAllocationValidator {
    return StrictBrokerAllocationValidator(
        mutableListOf<StrictBrokerAllocationRule>(
            StrictBrokerAllocationAmountRangeRule(4.6, 2954.4),
            StrictBrokerAllocationCodePrefixRule("X"),
            StrictBrokerAllocationMaximumRangeRule(1.2, 3362.4),
            StrictBrokerAllocationPinnedGuardRule(3600),
            StrictBrokerAllocationCodeLengthRule(1000),
            StrictBrokerAllocationRequiredTitleRule("strict broker allocation needs this field"),
        ),
    )
}
