package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.kit.text.normalize

/** A rule a weighted backhaul threshold must pass. Check returns its problems, or none. */
interface WeightedBackhaulThresholdRule {
    fun check(item: WeightedBackhaulThreshold): MutableList<String>
}

data class WeightedBackhaulThresholdAllowedCountryRule(
    var allowed: MutableList<String>,
) : WeightedBackhaulThresholdRule {
    override fun check(item: WeightedBackhaulThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class WeightedBackhaulThresholdVersionRangeRule(
    var min: Int,
    var max: Int,
) : WeightedBackhaulThresholdRule {
    override fun check(item: WeightedBackhaulThreshold): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class WeightedBackhaulThresholdUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : WeightedBackhaulThresholdRule {
    override fun check(item: WeightedBackhaulThreshold): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedBackhaulThresholdShiftCodeLengthRule(var maxLength: Int) : WeightedBackhaulThresholdRule {
    override fun check(item: WeightedBackhaulThreshold): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedBackhaulThresholdNameLengthRule(var maxLength: Int) : WeightedBackhaulThresholdRule {
    override fun check(item: WeightedBackhaulThreshold): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedBackhaulThresholdRequiredCountryRule(var message: String) : WeightedBackhaulThresholdRule {
    override fun check(item: WeightedBackhaulThreshold): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted backhaul threshold. */
data class WeightedBackhaulThresholdValidator(var rules: MutableList<WeightedBackhaulThresholdRule>) {
    fun validate(item: WeightedBackhaulThreshold): MutableList<String> {
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

    fun requireValid(item: WeightedBackhaulThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedBackhaulThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedBackhaulThresholdValidator(): WeightedBackhaulThresholdValidator {
    return WeightedBackhaulThresholdValidator(
        mutableListOf<WeightedBackhaulThresholdRule>(
            WeightedBackhaulThresholdAllowedCountryRule(mutableListOf<String>()),
            WeightedBackhaulThresholdVersionRangeRule(255, 3600),
            WeightedBackhaulThresholdUpdatedAtRangeRule(120, 255),
            WeightedBackhaulThresholdShiftCodeLengthRule(64),
            WeightedBackhaulThresholdNameLengthRule(3600),
            WeightedBackhaulThresholdRequiredCountryRule("weighted backhaul threshold needs an entry"),
        ),
    )
}
