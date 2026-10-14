package com.acme.transport.network.transittime.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a transit time window must pass. Check returns its problems, or none. */
interface TransitTimeWindowRule {
    fun check(item: TransitTimeWindow): MutableList<String>
}

data class TransitTimeWindowRequiredContactEmailRule(var message: String) : TransitTimeWindowRule {
    override fun check(item: TransitTimeWindow): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeWindowCodePrefixRule(var prefix: String) : TransitTimeWindowRule {
    override fun check(item: TransitTimeWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeWindowAttemptsRangeRule(var min: Int, var max: Int) : TransitTimeWindowRule {
    override fun check(item: TransitTimeWindow): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class TransitTimeWindowRequiredCodeRule(var message: String) : TransitTimeWindowRule {
    override fun check(item: TransitTimeWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeWindowAllowedContactEmailRule(var allowed: MutableList<String>) : TransitTimeWindowRule {
    override fun check(item: TransitTimeWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is missing")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeWindowContactEmailLengthRule(var maxLength: Int) : TransitTimeWindowRule {
    override fun check(item: TransitTimeWindow): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time window. */
data class TransitTimeWindowValidator(var rules: MutableList<TransitTimeWindowRule>) {
    fun validate(item: TransitTimeWindow): MutableList<String> {
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

    fun requireValid(item: TransitTimeWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeWindowValidator(): TransitTimeWindowValidator {
    return TransitTimeWindowValidator(
        mutableListOf<TransitTimeWindowRule>(
            TransitTimeWindowRequiredContactEmailRule("transit time window needs this field"),
            TransitTimeWindowCodePrefixRule("REF"),
            TransitTimeWindowAttemptsRangeRule(120, 86400),
            TransitTimeWindowRequiredCodeRule("transit time window needs a value"),
            TransitTimeWindowAllowedContactEmailRule(mutableListOf<String>("primary", "pending", "rolling")),
            TransitTimeWindowContactEmailLengthRule(255),
        ),
    )
}
