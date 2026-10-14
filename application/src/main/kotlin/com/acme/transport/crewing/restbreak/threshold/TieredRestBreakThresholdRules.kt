package com.acme.transport.crewing.restbreak.threshold

import com.acme.transport.kit.text.normalize

/** A rule a tiered rest break threshold must pass. Check returns its problems, or none. */
interface TieredRestBreakThresholdRule {
    fun check(item: TieredRestBreakThreshold): MutableList<String>
}

data class TieredRestBreakThresholdRequiredTitleRule(var message: String) : TieredRestBreakThresholdRule {
    override fun check(item: TieredRestBreakThreshold): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredRestBreakThresholdCodeLengthRule(var maxLength: Int) : TieredRestBreakThresholdRule {
    override fun check(item: TieredRestBreakThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredRestBreakThresholdAllowedCodeRule(
    var allowed: MutableList<String>,
) : TieredRestBreakThresholdRule {
    override fun check(item: TieredRestBreakThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class TieredRestBreakThresholdCodePrefixRule(var prefix: String) : TieredRestBreakThresholdRule {
    override fun check(item: TieredRestBreakThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredRestBreakThresholdMinimumRangeRule(
    var min: Double,
    var max: Double,
) : TieredRestBreakThresholdRule {
    override fun check(item: TieredRestBreakThreshold): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered rest break threshold. */
data class TieredRestBreakThresholdValidator(var rules: MutableList<TieredRestBreakThresholdRule>) {
    fun validate(item: TieredRestBreakThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredRestBreakThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredRestBreakThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredRestBreakThresholdValidator(): TieredRestBreakThresholdValidator {
    return TieredRestBreakThresholdValidator(
        mutableListOf<TieredRestBreakThresholdRule>(
            TieredRestBreakThresholdRequiredTitleRule("tiered rest break threshold needs an entry"),
            TieredRestBreakThresholdCodeLengthRule(1000),
            TieredRestBreakThresholdAllowedCodeRule(mutableListOf<String>("pending")),
            TieredRestBreakThresholdCodePrefixRule("X"),
            TieredRestBreakThresholdMinimumRangeRule(4.9, 973.3),
        ),
    )
}
