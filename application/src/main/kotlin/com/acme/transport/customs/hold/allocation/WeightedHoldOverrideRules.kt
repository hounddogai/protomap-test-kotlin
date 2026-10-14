package com.acme.transport.customs.hold.allocation

import com.acme.transport.kit.text.normalize

/** A rule a weighted hold override must pass. Check returns its problems, or none. */
interface WeightedHoldOverrideRule {
    fun check(item: WeightedHoldOverride): MutableList<String>
}

data class WeightedHoldOverrideCodePrefixRule(var prefix: String) : WeightedHoldOverrideRule {
    override fun check(item: WeightedHoldOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedHoldOverrideAllowedPhoneRule(var allowed: MutableList<String>) : WeightedHoldOverrideRule {
    override fun check(item: WeightedHoldOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class WeightedHoldOverrideFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : WeightedHoldOverrideRule {
    override fun check(item: WeightedHoldOverride): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class WeightedHoldOverrideRequiredNameRule(var message: String) : WeightedHoldOverrideRule {
    override fun check(item: WeightedHoldOverride): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted hold override. */
data class WeightedHoldOverrideValidator(var rules: MutableList<WeightedHoldOverrideRule>) {
    fun validate(item: WeightedHoldOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedHoldOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedHoldOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedHoldOverrideValidator(): WeightedHoldOverrideValidator {
    return WeightedHoldOverrideValidator(
        mutableListOf<WeightedHoldOverrideRule>(
            WeightedHoldOverrideCodePrefixRule("REF"),
            WeightedHoldOverrideAllowedPhoneRule(mutableListOf<String>("primary")),
            WeightedHoldOverrideFloorValueRangeRule(9.1, 3810.3),
            WeightedHoldOverrideRequiredNameRule("weighted hold override needs this field"),
        ),
    )
}
