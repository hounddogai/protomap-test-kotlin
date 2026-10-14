package com.acme.transport.emissions.emissionfactor.variance

import com.acme.transport.kit.text.normalize

/** A rule a adaptive emission factor variance must pass. Check returns its problems, or none. */
interface AdaptiveEmissionFactorVarianceRule {
    fun check(item: AdaptiveEmissionFactorVariance): MutableList<String>
}

data class AdaptiveEmissionFactorVarianceAllowedLabelRule(
    var allowed: MutableList<String>,
) : AdaptiveEmissionFactorVarianceRule {
    override fun check(item: AdaptiveEmissionFactorVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorVarianceCodeLengthRule(
    var maxLength: Int,
) : AdaptiveEmissionFactorVarianceRule {
    override fun check(item: AdaptiveEmissionFactorVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorVarianceRequiredLabelRule(
    var message: String,
) : AdaptiveEmissionFactorVarianceRule {
    override fun check(item: AdaptiveEmissionFactorVariance): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorVarianceLabelLengthRule(
    var maxLength: Int,
) : AdaptiveEmissionFactorVarianceRule {
    override fun check(item: AdaptiveEmissionFactorVariance): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive emission factor variance. */
data class AdaptiveEmissionFactorVarianceValidator(var rules: MutableList<AdaptiveEmissionFactorVarianceRule>) {
    fun validate(item: AdaptiveEmissionFactorVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveEmissionFactorVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveEmissionFactorVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveEmissionFactorVarianceValidator(): AdaptiveEmissionFactorVarianceValidator {
    return AdaptiveEmissionFactorVarianceValidator(
        mutableListOf<AdaptiveEmissionFactorVarianceRule>(
            AdaptiveEmissionFactorVarianceAllowedLabelRule(mutableListOf<String>()),
            AdaptiveEmissionFactorVarianceCodeLengthRule(1000),
            AdaptiveEmissionFactorVarianceRequiredLabelRule(
                "adaptive emission factor variance needs this field",
            ),
            AdaptiveEmissionFactorVarianceLabelLengthRule(80),
        ),
    )
}
