package com.acme.transport.emissions.emissionfactor.claim

/** A rule a weighted emission factor threshold must pass. Check returns its problems, or none. */
interface WeightedEmissionFactorThresholdRule {
    fun check(item: WeightedEmissionFactorThreshold): MutableList<String>
}

data class WeightedEmissionFactorThresholdRequiredLocaleRule(
    var message: String,
) : WeightedEmissionFactorThresholdRule {
    override fun check(item: WeightedEmissionFactorThreshold): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedEmissionFactorThresholdRequiredCodeRule(
    var message: String,
) : WeightedEmissionFactorThresholdRule {
    override fun check(item: WeightedEmissionFactorThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedEmissionFactorThresholdSequenceRangeRule(
    var min: Int,
    var max: Int,
) : WeightedEmissionFactorThresholdRule {
    override fun check(item: WeightedEmissionFactorThreshold): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class WeightedEmissionFactorThresholdCodePrefixRule(
    var prefix: String,
) : WeightedEmissionFactorThresholdRule {
    override fun check(item: WeightedEmissionFactorThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedEmissionFactorThresholdCodeLengthRule(
    var maxLength: Int,
) : WeightedEmissionFactorThresholdRule {
    override fun check(item: WeightedEmissionFactorThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted emission factor threshold. */
data class WeightedEmissionFactorThresholdValidator(
    var rules: MutableList<WeightedEmissionFactorThresholdRule>,
) {
    fun validate(item: WeightedEmissionFactorThreshold): MutableList<String> {
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

    fun requireValid(item: WeightedEmissionFactorThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedEmissionFactorThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedEmissionFactorThresholdValidator(): WeightedEmissionFactorThresholdValidator {
    return WeightedEmissionFactorThresholdValidator(
        mutableListOf<WeightedEmissionFactorThresholdRule>(
            WeightedEmissionFactorThresholdRequiredLocaleRule(
                "weighted emission factor threshold needs a value",
            ),
            WeightedEmissionFactorThresholdRequiredCodeRule("weighted emission factor threshold needs a value"),
            WeightedEmissionFactorThresholdSequenceRangeRule(64, 1000),
            WeightedEmissionFactorThresholdCodePrefixRule("REF"),
            WeightedEmissionFactorThresholdCodeLengthRule(32),
        ),
    )
}
