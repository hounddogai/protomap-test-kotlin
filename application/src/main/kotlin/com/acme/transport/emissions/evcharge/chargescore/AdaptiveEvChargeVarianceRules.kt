package com.acme.transport.emissions.evcharge.chargescore

import com.acme.transport.kit.text.normalize

/** A rule a adaptive ev charge variance must pass. Check returns its problems, or none. */
interface AdaptiveEvChargeVarianceRule {
    fun check(item: AdaptiveEvChargeVariance): MutableList<String>
}

data class AdaptiveEvChargeVarianceRequiredCodeRule(var message: String) : AdaptiveEvChargeVarianceRule {
    override fun check(item: AdaptiveEvChargeVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEvChargeVarianceLimitRangeRule(var min: Int, var max: Int) : AdaptiveEvChargeVarianceRule {
    override fun check(item: AdaptiveEvChargeVariance): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEvChargeVarianceCodeLengthRule(var maxLength: Int) : AdaptiveEvChargeVarianceRule {
    override fun check(item: AdaptiveEvChargeVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEvChargeVarianceRequiredTimezoneRule(var message: String) : AdaptiveEvChargeVarianceRule {
    override fun check(item: AdaptiveEvChargeVariance): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEvChargeVarianceRequiredNameRule(var message: String) : AdaptiveEvChargeVarianceRule {
    override fun check(item: AdaptiveEvChargeVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEvChargeVarianceAllowedNameRule(
    var allowed: MutableList<String>,
) : AdaptiveEvChargeVarianceRule {
    override fun check(item: AdaptiveEvChargeVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive ev charge variance. */
data class AdaptiveEvChargeVarianceValidator(var rules: MutableList<AdaptiveEvChargeVarianceRule>) {
    fun validate(item: AdaptiveEvChargeVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveEvChargeVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveEvChargeVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveEvChargeVarianceValidator(): AdaptiveEvChargeVarianceValidator {
    return AdaptiveEvChargeVarianceValidator(
        mutableListOf<AdaptiveEvChargeVarianceRule>(
            AdaptiveEvChargeVarianceRequiredCodeRule("adaptive ev charge variance needs a value"),
            AdaptiveEvChargeVarianceLimitRangeRule(255, 86400),
            AdaptiveEvChargeVarianceCodeLengthRule(64),
            AdaptiveEvChargeVarianceRequiredTimezoneRule("adaptive ev charge variance needs a value"),
            AdaptiveEvChargeVarianceRequiredNameRule("adaptive ev charge variance needs a value"),
            AdaptiveEvChargeVarianceAllowedNameRule(mutableListOf<String>("legacy", "tiered")),
        ),
    )
}
