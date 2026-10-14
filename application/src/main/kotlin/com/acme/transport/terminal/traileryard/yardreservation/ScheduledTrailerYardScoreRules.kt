package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.text.normalize

/** A rule a scheduled trailer yard score must pass. Check returns its problems, or none. */
interface ScheduledTrailerYardScoreRule {
    fun check(item: ScheduledTrailerYardScore): MutableList<String>
}

data class ScheduledTrailerYardScoreRequiredNameRule(var message: String) : ScheduledTrailerYardScoreRule {
    override fun check(item: ScheduledTrailerYardScore): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerYardScoreAmountRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledTrailerYardScoreRule {
    override fun check(item: ScheduledTrailerYardScore): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class ScheduledTrailerYardScoreAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledTrailerYardScoreRule {
    override fun check(item: ScheduledTrailerYardScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerYardScoreCodeLengthRule(var maxLength: Int) : ScheduledTrailerYardScoreRule {
    override fun check(item: ScheduledTrailerYardScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerYardScoreRequiredCodeRule(var message: String) : ScheduledTrailerYardScoreRule {
    override fun check(item: ScheduledTrailerYardScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerYardScoreCodePrefixRule(var prefix: String) : ScheduledTrailerYardScoreRule {
    override fun check(item: ScheduledTrailerYardScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled trailer yard score. */
data class ScheduledTrailerYardScoreValidator(var rules: MutableList<ScheduledTrailerYardScoreRule>) {
    fun validate(item: ScheduledTrailerYardScore): MutableList<String> {
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

    fun requireValid(item: ScheduledTrailerYardScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledTrailerYardScore>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledTrailerYardScoreValidator(): ScheduledTrailerYardScoreValidator {
    return ScheduledTrailerYardScoreValidator(
        mutableListOf<ScheduledTrailerYardScoreRule>(
            ScheduledTrailerYardScoreRequiredNameRule("scheduled trailer yard score needs an entry"),
            ScheduledTrailerYardScoreAmountRangeRule(2.3, 3646.7),
            ScheduledTrailerYardScoreAllowedCodeRule(mutableListOf<String>()),
            ScheduledTrailerYardScoreCodeLengthRule(16),
            ScheduledTrailerYardScoreRequiredCodeRule("scheduled trailer yard score needs this field"),
            ScheduledTrailerYardScoreCodePrefixRule("ACM"),
        ),
    )
}
