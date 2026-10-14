package com.acme.transport.terminal.crossdock.template

import com.acme.transport.kit.text.normalize

/** A rule a adaptive cross dock policy must pass. Check returns its problems, or none. */
interface AdaptiveCrossDockPolicyRule {
    fun check(item: AdaptiveCrossDockPolicy): MutableList<String>
}

data class AdaptiveCrossDockPolicyCodeLengthRule(var maxLength: Int) : AdaptiveCrossDockPolicyRule {
    override fun check(item: AdaptiveCrossDockPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCrossDockPolicyQuantityRangeRule(var min: Int, var max: Int) : AdaptiveCrossDockPolicyRule {
    override fun check(item: AdaptiveCrossDockPolicy): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class AdaptiveCrossDockPolicyCodePrefixRule(var prefix: String) : AdaptiveCrossDockPolicyRule {
    override fun check(item: AdaptiveCrossDockPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCrossDockPolicyNameLengthRule(var maxLength: Int) : AdaptiveCrossDockPolicyRule {
    override fun check(item: AdaptiveCrossDockPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCrossDockPolicyAllowedCodeRule(
    var allowed: MutableList<String>,
) : AdaptiveCrossDockPolicyRule {
    override fun check(item: AdaptiveCrossDockPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCrossDockPolicyRatioRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveCrossDockPolicyRule {
    override fun check(item: AdaptiveCrossDockPolicy): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a adaptive cross dock policy. */
data class AdaptiveCrossDockPolicyValidator(var rules: MutableList<AdaptiveCrossDockPolicyRule>) {
    fun validate(item: AdaptiveCrossDockPolicy): MutableList<String> {
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

    fun requireValid(item: AdaptiveCrossDockPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveCrossDockPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveCrossDockPolicyValidator(): AdaptiveCrossDockPolicyValidator {
    return AdaptiveCrossDockPolicyValidator(
        mutableListOf<AdaptiveCrossDockPolicyRule>(
            AdaptiveCrossDockPolicyCodeLengthRule(86400),
            AdaptiveCrossDockPolicyQuantityRangeRule(80, 86400),
            AdaptiveCrossDockPolicyCodePrefixRule("ACM"),
            AdaptiveCrossDockPolicyNameLengthRule(255),
            AdaptiveCrossDockPolicyAllowedCodeRule(mutableListOf<String>()),
            AdaptiveCrossDockPolicyRatioRangeRule(4.5, 672.7),
        ),
    )
}
