package com.acme.transport.network.node.nodescore

import com.acme.transport.kit.text.normalize

/** A rule a global node variance must pass. Check returns its problems, or none. */
interface GlobalNodeVarianceRule {
    fun check(item: GlobalNodeVariance): MutableList<String>
}

data class GlobalNodeVarianceAllowedSummaryRule(var allowed: MutableList<String>) : GlobalNodeVarianceRule {
    override fun check(item: GlobalNodeVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class GlobalNodeVarianceCodePrefixRule(var prefix: String) : GlobalNodeVarianceRule {
    override fun check(item: GlobalNodeVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalNodeVarianceCodeLengthRule(var maxLength: Int) : GlobalNodeVarianceRule {
    override fun check(item: GlobalNodeVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalNodeVarianceLaneLengthRule(var maxLength: Int) : GlobalNodeVarianceRule {
    override fun check(item: GlobalNodeVariance): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalNodeVarianceMaximumRangeRule(var min: Double, var max: Double) : GlobalNodeVarianceRule {
    override fun check(item: GlobalNodeVariance): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a global node variance. */
data class GlobalNodeVarianceValidator(var rules: MutableList<GlobalNodeVarianceRule>) {
    fun validate(item: GlobalNodeVariance): MutableList<String> {
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

    fun requireValid(item: GlobalNodeVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalNodeVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalNodeVarianceValidator(): GlobalNodeVarianceValidator {
    return GlobalNodeVarianceValidator(
        mutableListOf<GlobalNodeVarianceRule>(
            GlobalNodeVarianceAllowedSummaryRule(mutableListOf<String>()),
            GlobalNodeVarianceCodePrefixRule("X"),
            GlobalNodeVarianceCodeLengthRule(64),
            GlobalNodeVarianceLaneLengthRule(16),
            GlobalNodeVarianceMaximumRangeRule(6.3, 1136.4),
        ),
    )
}
