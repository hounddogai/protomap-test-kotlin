package com.acme.transport.emissions.emissionfactor.plan

import com.acme.transport.kit.text.normalize

/** A rule a emission factor threshold must pass. Check returns its problems, or none. */
interface EmissionFactorThresholdRule {
    fun check(item: EmissionFactorThreshold): MutableList<String>
}

data class EmissionFactorThresholdReviewerLengthRule(var maxLength: Int) : EmissionFactorThresholdRule {
    override fun check(item: EmissionFactorThreshold): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorThresholdLabelLengthRule(var maxLength: Int) : EmissionFactorThresholdRule {
    override fun check(item: EmissionFactorThreshold): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorThresholdPinnedGuardRule(var limit: Int) : EmissionFactorThresholdRule {
    override fun check(item: EmissionFactorThreshold): MutableList<String> {
        if (item.pinned && item.priority > limit) {
            return mutableListOf<String>("priority ${item.priority} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorThresholdRequiredReviewerRule(var message: String) : EmissionFactorThresholdRule {
    override fun check(item: EmissionFactorThreshold): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorThresholdFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : EmissionFactorThresholdRule {
    override fun check(item: EmissionFactorThreshold): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class EmissionFactorThresholdAllowedCodeRule(
    var allowed: MutableList<String>,
) : EmissionFactorThresholdRule {
    override fun check(item: EmissionFactorThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor threshold. */
data class EmissionFactorThresholdValidator(var rules: MutableList<EmissionFactorThresholdRule>) {
    fun validate(item: EmissionFactorThreshold): MutableList<String> {
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

    fun requireValid(item: EmissionFactorThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorThresholdValidator(): EmissionFactorThresholdValidator {
    return EmissionFactorThresholdValidator(
        mutableListOf<EmissionFactorThresholdRule>(
            EmissionFactorThresholdReviewerLengthRule(1000),
            EmissionFactorThresholdLabelLengthRule(80),
            EmissionFactorThresholdPinnedGuardRule(32),
            EmissionFactorThresholdRequiredReviewerRule("emission factor threshold needs this field"),
            EmissionFactorThresholdFuelLitersRangeRule(7.7, 4305.1),
            EmissionFactorThresholdAllowedCodeRule(mutableListOf<String>("weighted", "rolling")),
        ),
    )
}
