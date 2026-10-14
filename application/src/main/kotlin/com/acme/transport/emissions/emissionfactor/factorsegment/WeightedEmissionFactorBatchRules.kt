package com.acme.transport.emissions.emissionfactor.factorsegment

import com.acme.transport.kit.text.normalize

/** A rule a weighted emission factor batch must pass. Check returns its problems, or none. */
interface WeightedEmissionFactorBatchRule {
    fun check(item: WeightedEmissionFactorBatch): MutableList<String>
}

data class WeightedEmissionFactorBatchEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : WeightedEmissionFactorBatchRule {
    override fun check(item: WeightedEmissionFactorBatch): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedEmissionFactorBatchRequiredNameRule(var message: String) : WeightedEmissionFactorBatchRule {
    override fun check(item: WeightedEmissionFactorBatch): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedEmissionFactorBatchAllowedNameRule(
    var allowed: MutableList<String>,
) : WeightedEmissionFactorBatchRule {
    override fun check(item: WeightedEmissionFactorBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted emission factor batch. */
data class WeightedEmissionFactorBatchValidator(var rules: MutableList<WeightedEmissionFactorBatchRule>) {
    fun validate(item: WeightedEmissionFactorBatch): MutableList<String> {
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

    fun requireValid(item: WeightedEmissionFactorBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedEmissionFactorBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedEmissionFactorBatchValidator(): WeightedEmissionFactorBatchValidator {
    return WeightedEmissionFactorBatchValidator(
        mutableListOf<WeightedEmissionFactorBatchRule>(
            WeightedEmissionFactorBatchEtaMinutesRangeRule(16, 120),
            WeightedEmissionFactorBatchRequiredNameRule("weighted emission factor batch needs this field"),
            WeightedEmissionFactorBatchAllowedNameRule(mutableListOf<String>()),
        ),
    )
}
