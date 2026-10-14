package com.acme.transport.freightpay.ratecard.policy

import com.acme.transport.kit.text.normalize

/** A rule a weighted rate card threshold must pass. Check returns its problems, or none. */
interface WeightedRateCardThresholdRule {
    fun check(item: WeightedRateCardThreshold): MutableList<String>
}

data class WeightedRateCardThresholdRequiredExternalRefRule(
    var message: String,
) : WeightedRateCardThresholdRule {
    override fun check(item: WeightedRateCardThreshold): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedRateCardThresholdOdometerRangeRule(
    var min: Int,
    var max: Int,
) : WeightedRateCardThresholdRule {
    override fun check(item: WeightedRateCardThreshold): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class WeightedRateCardThresholdAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : WeightedRateCardThresholdRule {
    override fun check(item: WeightedRateCardThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted rate card threshold. */
data class WeightedRateCardThresholdValidator(var rules: MutableList<WeightedRateCardThresholdRule>) {
    fun validate(item: WeightedRateCardThreshold): MutableList<String> {
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

    fun requireValid(item: WeightedRateCardThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedRateCardThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedRateCardThresholdValidator(): WeightedRateCardThresholdValidator {
    return WeightedRateCardThresholdValidator(
        mutableListOf<WeightedRateCardThresholdRule>(
            WeightedRateCardThresholdRequiredExternalRefRule("weighted rate card threshold needs a value"),
            WeightedRateCardThresholdOdometerRangeRule(16, 32),
            WeightedRateCardThresholdAllowedExternalRefRule(mutableListOf<String>("pending", "capped")),
        ),
    )
}
