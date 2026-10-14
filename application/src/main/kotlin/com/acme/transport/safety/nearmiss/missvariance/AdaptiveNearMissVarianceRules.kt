package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.kit.text.normalize

/** A rule a adaptive near miss variance must pass. Check returns its problems, or none. */
interface AdaptiveNearMissVarianceRule {
    fun check(item: AdaptiveNearMissVariance): MutableList<String>
}

data class AdaptiveNearMissVarianceCodeLengthRule(var maxLength: Int) : AdaptiveNearMissVarianceRule {
    override fun check(item: AdaptiveNearMissVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNearMissVarianceRequiredCodeRule(var message: String) : AdaptiveNearMissVarianceRule {
    override fun check(item: AdaptiveNearMissVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNearMissVariancePalletsRangeRule(var min: Int, var max: Int) : AdaptiveNearMissVarianceRule {
    override fun check(item: AdaptiveNearMissVariance): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNearMissVarianceRequiredLabelRule(var message: String) : AdaptiveNearMissVarianceRule {
    override fun check(item: AdaptiveNearMissVariance): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNearMissVarianceRequiredOwnerIdRule(var message: String) : AdaptiveNearMissVarianceRule {
    override fun check(item: AdaptiveNearMissVariance): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNearMissVarianceAllowedLabelRule(
    var allowed: MutableList<String>,
) : AdaptiveNearMissVarianceRule {
    override fun check(item: AdaptiveNearMissVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive near miss variance. */
data class AdaptiveNearMissVarianceValidator(var rules: MutableList<AdaptiveNearMissVarianceRule>) {
    fun validate(item: AdaptiveNearMissVariance): MutableList<String> {
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

    fun requireValid(item: AdaptiveNearMissVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveNearMissVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveNearMissVarianceValidator(): AdaptiveNearMissVarianceValidator {
    return AdaptiveNearMissVarianceValidator(
        mutableListOf<AdaptiveNearMissVarianceRule>(
            AdaptiveNearMissVarianceCodeLengthRule(80),
            AdaptiveNearMissVarianceRequiredCodeRule("adaptive near miss variance needs this field"),
            AdaptiveNearMissVariancePalletsRangeRule(32, 64),
            AdaptiveNearMissVarianceRequiredLabelRule("adaptive near miss variance needs this field"),
            AdaptiveNearMissVarianceRequiredOwnerIdRule("adaptive near miss variance needs this field"),
            AdaptiveNearMissVarianceAllowedLabelRule(mutableListOf<String>("manual")),
        ),
    )
}
