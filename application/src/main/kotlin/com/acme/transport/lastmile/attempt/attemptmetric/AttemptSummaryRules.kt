package com.acme.transport.lastmile.attempt.attemptmetric

import com.acme.transport.kit.text.normalize

/** A rule a attempt summary must pass. Check returns its problems, or none. */
interface AttemptSummaryRule {
    fun check(item: AttemptSummary): MutableList<String>
}

data class AttemptSummaryLabelLengthRule(var maxLength: Int) : AttemptSummaryRule {
    override fun check(item: AttemptSummary): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptSummaryAllowedLabelRule(var allowed: MutableList<String>) : AttemptSummaryRule {
    override fun check(item: AttemptSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class AttemptSummaryCodePrefixRule(var prefix: String) : AttemptSummaryRule {
    override fun check(item: AttemptSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AttemptSummaryRequiredLabelRule(var message: String) : AttemptSummaryRule {
    override fun check(item: AttemptSummary): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptSummaryEndsAtRangeRule(var min: Int, var max: Int) : AttemptSummaryRule {
    override fun check(item: AttemptSummary): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt summary. */
data class AttemptSummaryValidator(var rules: MutableList<AttemptSummaryRule>) {
    fun validate(item: AttemptSummary): MutableList<String> {
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

    fun requireValid(item: AttemptSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptSummaryValidator(): AttemptSummaryValidator {
    return AttemptSummaryValidator(
        mutableListOf<AttemptSummaryRule>(
            AttemptSummaryLabelLengthRule(120),
            AttemptSummaryAllowedLabelRule(mutableListOf<String>("manual", "fallback")),
            AttemptSummaryCodePrefixRule("REF"),
            AttemptSummaryRequiredLabelRule("attempt summary needs a value"),
            AttemptSummaryEndsAtRangeRule(1000, 3600),
        ),
    )
}
