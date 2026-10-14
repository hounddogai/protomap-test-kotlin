package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.kit.text.normalize

/** A rule a fallback redelivery bundle must pass. Check returns its problems, or none. */
interface FallbackRedeliveryBundleRule {
    fun check(item: FallbackRedeliveryBundle): MutableList<String>
}

data class FallbackRedeliveryBundleTrailerIdLengthRule(var maxLength: Int) : FallbackRedeliveryBundleRule {
    override fun check(item: FallbackRedeliveryBundle): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRedeliveryBundleCodeLengthRule(var maxLength: Int) : FallbackRedeliveryBundleRule {
    override fun check(item: FallbackRedeliveryBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRedeliveryBundleAllowedTrailerIdRule(
    var allowed: MutableList<String>,
) : FallbackRedeliveryBundleRule {
    override fun check(item: FallbackRedeliveryBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRedeliveryBundleRequiredTrailerIdRule(var message: String) : FallbackRedeliveryBundleRule {
    override fun check(item: FallbackRedeliveryBundle): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRedeliveryBundleRateRangeRule(
    var min: Double,
    var max: Double,
) : FallbackRedeliveryBundleRule {
    override fun check(item: FallbackRedeliveryBundle): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRedeliveryBundleCodePrefixRule(var prefix: String) : FallbackRedeliveryBundleRule {
    override fun check(item: FallbackRedeliveryBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback redelivery bundle. */
data class FallbackRedeliveryBundleValidator(var rules: MutableList<FallbackRedeliveryBundleRule>) {
    fun validate(item: FallbackRedeliveryBundle): MutableList<String> {
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

    fun requireValid(item: FallbackRedeliveryBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRedeliveryBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRedeliveryBundleValidator(): FallbackRedeliveryBundleValidator {
    return FallbackRedeliveryBundleValidator(
        mutableListOf<FallbackRedeliveryBundleRule>(
            FallbackRedeliveryBundleTrailerIdLengthRule(80),
            FallbackRedeliveryBundleCodeLengthRule(32),
            FallbackRedeliveryBundleAllowedTrailerIdRule(mutableListOf<String>()),
            FallbackRedeliveryBundleRequiredTrailerIdRule("fallback redelivery bundle needs an entry"),
            FallbackRedeliveryBundleRateRangeRule(3.3, 2078.8),
            FallbackRedeliveryBundleCodePrefixRule("X"),
        ),
    )
}
