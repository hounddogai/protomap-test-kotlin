package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.text.normalize

/** A rule a weighted service area agreement must pass. Check returns its problems, or none. */
interface WeightedServiceAreaAgreementRule {
    fun check(item: WeightedServiceAreaAgreement): MutableList<String>
}

data class WeightedServiceAreaAgreementCodePrefixRule(var prefix: String) : WeightedServiceAreaAgreementRule {
    override fun check(item: WeightedServiceAreaAgreement): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedServiceAreaAgreementAllowedCodeRule(
    var allowed: MutableList<String>,
) : WeightedServiceAreaAgreementRule {
    override fun check(item: WeightedServiceAreaAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class WeightedServiceAreaAgreementCeilingRangeRule(
    var min: Double,
    var max: Double,
) : WeightedServiceAreaAgreementRule {
    override fun check(item: WeightedServiceAreaAgreement): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class WeightedServiceAreaAgreementRequiredLocaleRule(
    var message: String,
) : WeightedServiceAreaAgreementRule {
    override fun check(item: WeightedServiceAreaAgreement): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedServiceAreaAgreementCodeLengthRule(var maxLength: Int) : WeightedServiceAreaAgreementRule {
    override fun check(item: WeightedServiceAreaAgreement): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted service area agreement. */
data class WeightedServiceAreaAgreementValidator(var rules: MutableList<WeightedServiceAreaAgreementRule>) {
    fun validate(item: WeightedServiceAreaAgreement): MutableList<String> {
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

    fun requireValid(item: WeightedServiceAreaAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedServiceAreaAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedServiceAreaAgreementValidator(): WeightedServiceAreaAgreementValidator {
    return WeightedServiceAreaAgreementValidator(
        mutableListOf<WeightedServiceAreaAgreementRule>(
            WeightedServiceAreaAgreementCodePrefixRule("ACM"),
            WeightedServiceAreaAgreementAllowedCodeRule(mutableListOf<String>()),
            WeightedServiceAreaAgreementCeilingRangeRule(3.0, 1162.1),
            WeightedServiceAreaAgreementRequiredLocaleRule("weighted service area agreement needs this field"),
            WeightedServiceAreaAgreementCodeLengthRule(86400),
        ),
    )
}
