package com.acme.transport.linehaul.tender.variance

import com.acme.transport.kit.text.normalize

/** A rule a manual tender variance must pass. Check returns its problems, or none. */
interface ManualTenderVarianceRule {
    fun check(item: ManualTenderVariance): MutableList<String>
}

data class ManualTenderVarianceLabelLengthRule(var maxLength: Int) : ManualTenderVarianceRule {
    override fun check(item: ManualTenderVariance): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTenderVarianceBudgetRangeRule(var min: Double, var max: Double) : ManualTenderVarianceRule {
    override fun check(item: ManualTenderVariance): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualTenderVarianceRequiredLabelRule(var message: String) : ManualTenderVarianceRule {
    override fun check(item: ManualTenderVariance): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTenderVarianceDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : ManualTenderVarianceRule {
    override fun check(item: ManualTenderVariance): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualTenderVarianceAllowedCurrencyRule(
    var allowed: MutableList<String>,
) : ManualTenderVarianceRule {
    override fun check(item: ManualTenderVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.currency))) {
            return mutableListOf<String>("currency ${item.currency} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ManualTenderVarianceCodePrefixRule(var prefix: String) : ManualTenderVarianceRule {
    override fun check(item: ManualTenderVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual tender variance. */
data class ManualTenderVarianceValidator(var rules: MutableList<ManualTenderVarianceRule>) {
    fun validate(item: ManualTenderVariance): MutableList<String> {
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

    fun requireValid(item: ManualTenderVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTenderVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTenderVarianceValidator(): ManualTenderVarianceValidator {
    return ManualTenderVarianceValidator(
        mutableListOf<ManualTenderVarianceRule>(
            ManualTenderVarianceLabelLengthRule(255),
            ManualTenderVarianceBudgetRangeRule(6.0, 3189.3),
            ManualTenderVarianceRequiredLabelRule("manual tender variance needs a value"),
            ManualTenderVarianceDistanceKmRangeRule(1.1, 2221.4),
            ManualTenderVarianceAllowedCurrencyRule(mutableListOf<String>("tiered", "manual")),
            ManualTenderVarianceCodePrefixRule("OPS"),
        ),
    )
}
