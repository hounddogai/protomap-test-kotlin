package com.acme.transport.network.zone.entry

/** A rule a adaptive zone allocation must pass. Check returns its problems, or none. */
interface AdaptiveZoneAllocationRule {
    fun check(item: AdaptiveZoneAllocation): MutableList<String>
}

data class AdaptiveZoneAllocationRequiredSourceRule(var message: String) : AdaptiveZoneAllocationRule {
    override fun check(item: AdaptiveZoneAllocation): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveZoneAllocationSourceLengthRule(var maxLength: Int) : AdaptiveZoneAllocationRule {
    override fun check(item: AdaptiveZoneAllocation): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveZoneAllocationLabelLengthRule(var maxLength: Int) : AdaptiveZoneAllocationRule {
    override fun check(item: AdaptiveZoneAllocation): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveZoneAllocationRequiredCodeRule(var message: String) : AdaptiveZoneAllocationRule {
    override fun check(item: AdaptiveZoneAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveZoneAllocationCostRangeRule(var min: Double, var max: Double) : AdaptiveZoneAllocationRule {
    override fun check(item: AdaptiveZoneAllocation): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveZoneAllocationRevisionRangeRule(var min: Int, var max: Int) : AdaptiveZoneAllocationRule {
    override fun check(item: AdaptiveZoneAllocation): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a adaptive zone allocation. */
data class AdaptiveZoneAllocationValidator(var rules: MutableList<AdaptiveZoneAllocationRule>) {
    fun validate(item: AdaptiveZoneAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveZoneAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveZoneAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveZoneAllocationValidator(): AdaptiveZoneAllocationValidator {
    return AdaptiveZoneAllocationValidator(
        mutableListOf<AdaptiveZoneAllocationRule>(
            AdaptiveZoneAllocationRequiredSourceRule("adaptive zone allocation needs a value"),
            AdaptiveZoneAllocationSourceLengthRule(255),
            AdaptiveZoneAllocationLabelLengthRule(16),
            AdaptiveZoneAllocationRequiredCodeRule("adaptive zone allocation needs this field"),
            AdaptiveZoneAllocationCostRangeRule(4.2, 4123.0),
            AdaptiveZoneAllocationRevisionRangeRule(80, 1000),
        ),
    )
}
