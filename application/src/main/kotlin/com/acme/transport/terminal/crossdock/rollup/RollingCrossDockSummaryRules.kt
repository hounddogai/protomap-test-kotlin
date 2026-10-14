package com.acme.transport.terminal.crossdock.rollup

import com.acme.transport.kit.text.normalize

/** A rule a rolling cross dock summary must pass. Check returns its problems, or none. */
interface RollingCrossDockSummaryRule {
    fun check(item: RollingCrossDockSummary): MutableList<String>
}

data class RollingCrossDockSummaryWeightRangeRule(
    var min: Double,
    var max: Double,
) : RollingCrossDockSummaryRule {
    override fun check(item: RollingCrossDockSummary): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingCrossDockSummaryExternalRefLengthRule(var maxLength: Int) : RollingCrossDockSummaryRule {
    override fun check(item: RollingCrossDockSummary): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCrossDockSummaryCodeLengthRule(var maxLength: Int) : RollingCrossDockSummaryRule {
    override fun check(item: RollingCrossDockSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCrossDockSummaryAllowedCodeRule(
    var allowed: MutableList<String>,
) : RollingCrossDockSummaryRule {
    override fun check(item: RollingCrossDockSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class RollingCrossDockSummaryCodePrefixRule(var prefix: String) : RollingCrossDockSummaryRule {
    override fun check(item: RollingCrossDockSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling cross dock summary. */
data class RollingCrossDockSummaryValidator(var rules: MutableList<RollingCrossDockSummaryRule>) {
    fun validate(item: RollingCrossDockSummary): MutableList<String> {
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

    fun requireValid(item: RollingCrossDockSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingCrossDockSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingCrossDockSummaryValidator(): RollingCrossDockSummaryValidator {
    return RollingCrossDockSummaryValidator(
        mutableListOf<RollingCrossDockSummaryRule>(
            RollingCrossDockSummaryWeightRangeRule(7.4, 3150.4),
            RollingCrossDockSummaryExternalRefLengthRule(120),
            RollingCrossDockSummaryCodeLengthRule(86400),
            RollingCrossDockSummaryAllowedCodeRule(mutableListOf<String>("capped")),
            RollingCrossDockSummaryCodePrefixRule("ACM"),
        ),
    )
}
