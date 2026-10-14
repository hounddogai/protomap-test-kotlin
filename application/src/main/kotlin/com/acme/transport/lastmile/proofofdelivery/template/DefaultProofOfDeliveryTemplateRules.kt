package com.acme.transport.lastmile.proofofdelivery.template

import com.acme.transport.kit.text.normalize

/** A rule a default proof of delivery template must pass. Check returns its problems, or none. */
interface DefaultProofOfDeliveryTemplateRule {
    fun check(item: DefaultProofOfDeliveryTemplate): MutableList<String>
}

data class DefaultProofOfDeliveryTemplateUnitLengthRule(
    var maxLength: Int,
) : DefaultProofOfDeliveryTemplateRule {
    override fun check(item: DefaultProofOfDeliveryTemplate): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultProofOfDeliveryTemplateRequiredNameRule(
    var message: String,
) : DefaultProofOfDeliveryTemplateRule {
    override fun check(item: DefaultProofOfDeliveryTemplate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultProofOfDeliveryTemplateAllowedNameRule(
    var allowed: MutableList<String>,
) : DefaultProofOfDeliveryTemplateRule {
    override fun check(item: DefaultProofOfDeliveryTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class DefaultProofOfDeliveryTemplateCodePrefixRule(
    var prefix: String,
) : DefaultProofOfDeliveryTemplateRule {
    override fun check(item: DefaultProofOfDeliveryTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultProofOfDeliveryTemplateMinimumRangeRule(
    var min: Double,
    var max: Double,
) : DefaultProofOfDeliveryTemplateRule {
    override fun check(item: DefaultProofOfDeliveryTemplate): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a default proof of delivery template. */
data class DefaultProofOfDeliveryTemplateValidator(var rules: MutableList<DefaultProofOfDeliveryTemplateRule>) {
    fun validate(item: DefaultProofOfDeliveryTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultProofOfDeliveryTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultProofOfDeliveryTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultProofOfDeliveryTemplateValidator(): DefaultProofOfDeliveryTemplateValidator {
    return DefaultProofOfDeliveryTemplateValidator(
        mutableListOf<DefaultProofOfDeliveryTemplateRule>(
            DefaultProofOfDeliveryTemplateUnitLengthRule(80),
            DefaultProofOfDeliveryTemplateRequiredNameRule("default proof of delivery template needs an entry"),
            DefaultProofOfDeliveryTemplateAllowedNameRule(mutableListOf<String>("seasonal", "capped")),
            DefaultProofOfDeliveryTemplateCodePrefixRule("REF"),
            DefaultProofOfDeliveryTemplateMinimumRangeRule(0.5, 3398.8),
        ),
    )
}
