package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.kit.text.normalize

/** A rule a default redelivery bundle must pass. Check returns its problems, or none. */
interface DefaultRedeliveryBundleRule {
    fun check(item: DefaultRedeliveryBundle): MutableList<String>
}

data class DefaultRedeliveryBundleCeilingRangeRule(
    var min: Double,
    var max: Double,
) : DefaultRedeliveryBundleRule {
    override fun check(item: DefaultRedeliveryBundle): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRedeliveryBundleRouteCodeLengthRule(var maxLength: Int) : DefaultRedeliveryBundleRule {
    override fun check(item: DefaultRedeliveryBundle): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRedeliveryBundleAllowedContactEmailRule(
    var allowed: MutableList<String>,
) : DefaultRedeliveryBundleRule {
    override fun check(item: DefaultRedeliveryBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default redelivery bundle. */
data class DefaultRedeliveryBundleValidator(var rules: MutableList<DefaultRedeliveryBundleRule>) {
    fun validate(item: DefaultRedeliveryBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultRedeliveryBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRedeliveryBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRedeliveryBundleValidator(): DefaultRedeliveryBundleValidator {
    return DefaultRedeliveryBundleValidator(
        mutableListOf<DefaultRedeliveryBundleRule>(
            DefaultRedeliveryBundleCeilingRangeRule(7.3, 2775.7),
            DefaultRedeliveryBundleRouteCodeLengthRule(86400),
            DefaultRedeliveryBundleAllowedContactEmailRule(mutableListOf<String>()),
        ),
    )
}
