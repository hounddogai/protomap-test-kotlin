package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.kit.text.normalize

/** A rule a attempt baseline must pass. Check returns its problems, or none. */
interface AttemptBaselineRule {
    fun check(item: AttemptBaseline): MutableList<String>
}

data class AttemptBaselineAmountRangeRule(var min: Double, var max: Double) : AttemptBaselineRule {
    override fun check(item: AttemptBaseline): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AttemptBaselineAllowedSegmentRule(var allowed: MutableList<String>) : AttemptBaselineRule {
    override fun check(item: AttemptBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class AttemptBaselineCodePrefixRule(var prefix: String) : AttemptBaselineRule {
    override fun check(item: AttemptBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt baseline. */
data class AttemptBaselineValidator(var rules: MutableList<AttemptBaselineRule>) {
    fun validate(item: AttemptBaseline): MutableList<String> {
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

    fun requireValid(item: AttemptBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptBaselineValidator(): AttemptBaselineValidator {
    return AttemptBaselineValidator(
        mutableListOf<AttemptBaselineRule>(
            AttemptBaselineAmountRangeRule(0.3, 2267.7),
            AttemptBaselineAllowedSegmentRule(mutableListOf<String>("strict", "scheduled", "legacy")),
            AttemptBaselineCodePrefixRule("OPS"),
        ),
    )
}
