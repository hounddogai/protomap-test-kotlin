package com.acme.transport.linehaul.trailer.summary

import com.acme.transport.kit.text.normalize

/** A rule a weighted trailer mapping must pass. Check returns its problems, or none. */
interface WeightedTrailerMappingRule {
    fun check(item: WeightedTrailerMapping): MutableList<String>
}

data class WeightedTrailerMappingAmountRangeRule(
    var min: Double,
    var max: Double,
) : WeightedTrailerMappingRule {
    override fun check(item: WeightedTrailerMapping): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTrailerMappingCostRangeRule(var min: Double, var max: Double) : WeightedTrailerMappingRule {
    override fun check(item: WeightedTrailerMapping): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class WeightedTrailerMappingRequiredCodeRule(var message: String) : WeightedTrailerMappingRule {
    override fun check(item: WeightedTrailerMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedTrailerMappingCodePrefixRule(var prefix: String) : WeightedTrailerMappingRule {
    override fun check(item: WeightedTrailerMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTrailerMappingTitleLengthRule(var maxLength: Int) : WeightedTrailerMappingRule {
    override fun check(item: WeightedTrailerMapping): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTrailerMappingAllowedLaneRule(
    var allowed: MutableList<String>,
) : WeightedTrailerMappingRule {
    override fun check(item: WeightedTrailerMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted trailer mapping. */
data class WeightedTrailerMappingValidator(var rules: MutableList<WeightedTrailerMappingRule>) {
    fun validate(item: WeightedTrailerMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedTrailerMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedTrailerMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedTrailerMappingValidator(): WeightedTrailerMappingValidator {
    return WeightedTrailerMappingValidator(
        mutableListOf<WeightedTrailerMappingRule>(
            WeightedTrailerMappingAmountRangeRule(6.9, 1400.3),
            WeightedTrailerMappingCostRangeRule(0.5, 174.5),
            WeightedTrailerMappingRequiredCodeRule("weighted trailer mapping needs a value"),
            WeightedTrailerMappingCodePrefixRule("OPS"),
            WeightedTrailerMappingTitleLengthRule(32),
            WeightedTrailerMappingAllowedLaneRule(mutableListOf<String>("batched", "regional", "adaptive")),
        ),
    )
}
