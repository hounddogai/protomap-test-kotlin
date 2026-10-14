package com.acme.transport.linehaul.capacity.capacitypreference

import com.acme.transport.kit.text.normalize

/** A rule a weighted capacity preference must pass. Check returns its problems, or none. */
interface WeightedCapacityPreferenceRule {
    fun check(item: WeightedCapacityPreference): MutableList<String>
}

data class WeightedCapacityPreferenceScoreRangeRule(
    var min: Double,
    var max: Double,
) : WeightedCapacityPreferenceRule {
    override fun check(item: WeightedCapacityPreference): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class WeightedCapacityPreferenceChecksumLengthRule(var maxLength: Int) : WeightedCapacityPreferenceRule {
    override fun check(item: WeightedCapacityPreference): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCapacityPreferenceBudgetRangeRule(
    var min: Double,
    var max: Double,
) : WeightedCapacityPreferenceRule {
    override fun check(item: WeightedCapacityPreference): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class WeightedCapacityPreferenceAllowedCarrierRule(
    var allowed: MutableList<String>,
) : WeightedCapacityPreferenceRule {
    override fun check(item: WeightedCapacityPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class WeightedCapacityPreferenceSequenceRangeRule(
    var min: Int,
    var max: Int,
) : WeightedCapacityPreferenceRule {
    override fun check(item: WeightedCapacityPreference): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted capacity preference. */
data class WeightedCapacityPreferenceValidator(var rules: MutableList<WeightedCapacityPreferenceRule>) {
    fun validate(item: WeightedCapacityPreference): MutableList<String> {
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

    fun requireValid(item: WeightedCapacityPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedCapacityPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedCapacityPreferenceValidator(): WeightedCapacityPreferenceValidator {
    return WeightedCapacityPreferenceValidator(
        mutableListOf<WeightedCapacityPreferenceRule>(
            WeightedCapacityPreferenceScoreRangeRule(2.9, 3001.3),
            WeightedCapacityPreferenceChecksumLengthRule(120),
            WeightedCapacityPreferenceBudgetRangeRule(4.8, 2077.3),
            WeightedCapacityPreferenceAllowedCarrierRule(mutableListOf<String>("fallback")),
            WeightedCapacityPreferenceSequenceRangeRule(80, 255),
        ),
    )
}
