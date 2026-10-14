package com.acme.transport.linehaul.capacity.metric

/** A rule a capacity profile must pass. Check returns its problems, or none. */
interface CapacityProfileRule {
    fun check(item: CapacityProfile): MutableList<String>
}

data class CapacityProfileAmountRangeRule(var min: Double, var max: Double) : CapacityProfileRule {
    override fun check(item: CapacityProfile): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CapacityProfileCodePrefixRule(var prefix: String) : CapacityProfileRule {
    override fun check(item: CapacityProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CapacityProfileCeilingRangeRule(var min: Double, var max: Double) : CapacityProfileRule {
    override fun check(item: CapacityProfile): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capacity profile. */
data class CapacityProfileValidator(var rules: MutableList<CapacityProfileRule>) {
    fun validate(item: CapacityProfile): MutableList<String> {
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

    fun requireValid(item: CapacityProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityProfileValidator(): CapacityProfileValidator {
    return CapacityProfileValidator(
        mutableListOf<CapacityProfileRule>(
            CapacityProfileAmountRangeRule(6.9, 4022.1),
            CapacityProfileCodePrefixRule("X"),
            CapacityProfileCeilingRangeRule(9.5, 4735.4),
        ),
    )
}
