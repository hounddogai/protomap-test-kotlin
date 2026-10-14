package com.acme.transport.network.transittime.threshold

/** A rule a scheduled transit time entry must pass. Check returns its problems, or none. */
interface ScheduledTransitTimeEntryRule {
    fun check(item: ScheduledTransitTimeEntry): MutableList<String>
}

data class ScheduledTransitTimeEntryScoreRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledTransitTimeEntryRule {
    override fun check(item: ScheduledTransitTimeEntry): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class ScheduledTransitTimeEntryStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledTransitTimeEntryRule {
    override fun check(item: ScheduledTransitTimeEntry): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTransitTimeEntryCodePrefixRule(var prefix: String) : ScheduledTransitTimeEntryRule {
    override fun check(item: ScheduledTransitTimeEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled transit time entry. */
data class ScheduledTransitTimeEntryValidator(var rules: MutableList<ScheduledTransitTimeEntryRule>) {
    fun validate(item: ScheduledTransitTimeEntry): MutableList<String> {
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

    fun requireValid(item: ScheduledTransitTimeEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledTransitTimeEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledTransitTimeEntryValidator(): ScheduledTransitTimeEntryValidator {
    return ScheduledTransitTimeEntryValidator(
        mutableListOf<ScheduledTransitTimeEntryRule>(
            ScheduledTransitTimeEntryScoreRangeRule(0.8, 4496.6),
            ScheduledTransitTimeEntryStartsAtRangeRule(255, 86400),
            ScheduledTransitTimeEntryCodePrefixRule("ACM"),
        ),
    )
}
