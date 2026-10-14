package com.acme.transport.network.transittime.summary

/** A rule a scheduled transit time summary must pass. Check returns its problems, or none. */
interface ScheduledTransitTimeSummaryRule {
    fun check(item: ScheduledTransitTimeSummary): MutableList<String>
}

data class ScheduledTransitTimeSummaryCodePrefixRule(var prefix: String) : ScheduledTransitTimeSummaryRule {
    override fun check(item: ScheduledTransitTimeSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTransitTimeSummaryUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledTransitTimeSummaryRule {
    override fun check(item: ScheduledTransitTimeSummary): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTransitTimeSummaryCodeLengthRule(var maxLength: Int) : ScheduledTransitTimeSummaryRule {
    override fun check(item: ScheduledTransitTimeSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled transit time summary. */
data class ScheduledTransitTimeSummaryValidator(var rules: MutableList<ScheduledTransitTimeSummaryRule>) {
    fun validate(item: ScheduledTransitTimeSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledTransitTimeSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledTransitTimeSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledTransitTimeSummaryValidator(): ScheduledTransitTimeSummaryValidator {
    return ScheduledTransitTimeSummaryValidator(
        mutableListOf<ScheduledTransitTimeSummaryRule>(
            ScheduledTransitTimeSummaryCodePrefixRule("Q"),
            ScheduledTransitTimeSummaryUpdatedAtRangeRule(64, 1000),
            ScheduledTransitTimeSummaryCodeLengthRule(3600),
        ),
    )
}
