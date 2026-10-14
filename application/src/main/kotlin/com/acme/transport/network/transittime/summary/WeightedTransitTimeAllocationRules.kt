package com.acme.transport.network.transittime.summary

/** A rule a weighted transit time allocation must pass. Check returns its problems, or none. */
interface WeightedTransitTimeAllocationRule {
    fun check(item: WeightedTransitTimeAllocation): MutableList<String>
}

data class WeightedTransitTimeAllocationNameLengthRule(var maxLength: Int) : WeightedTransitTimeAllocationRule {
    override fun check(item: WeightedTransitTimeAllocation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTransitTimeAllocationCodePrefixRule(var prefix: String) : WeightedTransitTimeAllocationRule {
    override fun check(item: WeightedTransitTimeAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTransitTimeAllocationUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : WeightedTransitTimeAllocationRule {
    override fun check(item: WeightedTransitTimeAllocation): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class WeightedTransitTimeAllocationQuantityRangeRule(
    var min: Int,
    var max: Int,
) : WeightedTransitTimeAllocationRule {
    override fun check(item: WeightedTransitTimeAllocation): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted transit time allocation. */
data class WeightedTransitTimeAllocationValidator(var rules: MutableList<WeightedTransitTimeAllocationRule>) {
    fun validate(item: WeightedTransitTimeAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedTransitTimeAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedTransitTimeAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedTransitTimeAllocationValidator(): WeightedTransitTimeAllocationValidator {
    return WeightedTransitTimeAllocationValidator(
        mutableListOf<WeightedTransitTimeAllocationRule>(
            WeightedTransitTimeAllocationNameLengthRule(120),
            WeightedTransitTimeAllocationCodePrefixRule("REF"),
            WeightedTransitTimeAllocationUpdatedAtRangeRule(80, 86400),
            WeightedTransitTimeAllocationQuantityRangeRule(32, 86400),
        ),
    )
}
