package com.acme.transport.network.hub.hubsummary

/** A rule a lenient hub summary must pass. Check returns its problems, or none. */
interface LenientHubSummaryRule {
    fun check(item: LenientHubSummary): MutableList<String>
}

data class LenientHubSummaryTitleLengthRule(var maxLength: Int) : LenientHubSummaryRule {
    override fun check(item: LenientHubSummary): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientHubSummaryDurationMinutesRangeRule(var min: Int, var max: Int) : LenientHubSummaryRule {
    override fun check(item: LenientHubSummary): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientHubSummaryCodePrefixRule(var prefix: String) : LenientHubSummaryRule {
    override fun check(item: LenientHubSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient hub summary. */
data class LenientHubSummaryValidator(var rules: MutableList<LenientHubSummaryRule>) {
    fun validate(item: LenientHubSummary): MutableList<String> {
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

    fun requireValid(item: LenientHubSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientHubSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientHubSummaryValidator(): LenientHubSummaryValidator {
    return LenientHubSummaryValidator(
        mutableListOf<LenientHubSummaryRule>(
            LenientHubSummaryTitleLengthRule(120),
            LenientHubSummaryDurationMinutesRangeRule(16, 32),
            LenientHubSummaryCodePrefixRule("X"),
        ),
    )
}
