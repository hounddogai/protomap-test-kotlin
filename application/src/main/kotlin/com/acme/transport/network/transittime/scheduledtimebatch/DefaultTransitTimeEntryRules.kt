package com.acme.transport.network.transittime.scheduledtimebatch

import com.acme.transport.kit.text.normalize

/** A rule a default transit time entry must pass. Check returns its problems, or none. */
interface DefaultTransitTimeEntryRule {
    fun check(item: DefaultTransitTimeEntry): MutableList<String>
}

data class DefaultTransitTimeEntryVisibleGuardRule(var limit: Int) : DefaultTransitTimeEntryRule {
    override fun check(item: DefaultTransitTimeEntry): MutableList<String> {
        if (item.visible && item.startsAt > limit) {
            return mutableListOf<String>("starts at ${item.startsAt} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTransitTimeEntryMinimumRangeRule(
    var min: Double,
    var max: Double,
) : DefaultTransitTimeEntryRule {
    override fun check(item: DefaultTransitTimeEntry): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class DefaultTransitTimeEntryCodeLengthRule(var maxLength: Int) : DefaultTransitTimeEntryRule {
    override fun check(item: DefaultTransitTimeEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTransitTimeEntryRequiredLaneRule(var message: String) : DefaultTransitTimeEntryRule {
    override fun check(item: DefaultTransitTimeEntry): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultTransitTimeEntryTitleLengthRule(var maxLength: Int) : DefaultTransitTimeEntryRule {
    override fun check(item: DefaultTransitTimeEntry): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTransitTimeEntryAllowedLaneRule(
    var allowed: MutableList<String>,
) : DefaultTransitTimeEntryRule {
    override fun check(item: DefaultTransitTimeEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default transit time entry. */
data class DefaultTransitTimeEntryValidator(var rules: MutableList<DefaultTransitTimeEntryRule>) {
    fun validate(item: DefaultTransitTimeEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultTransitTimeEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTransitTimeEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTransitTimeEntryValidator(): DefaultTransitTimeEntryValidator {
    return DefaultTransitTimeEntryValidator(
        mutableListOf<DefaultTransitTimeEntryRule>(
            DefaultTransitTimeEntryVisibleGuardRule(32),
            DefaultTransitTimeEntryMinimumRangeRule(2.2, 2470.1),
            DefaultTransitTimeEntryCodeLengthRule(32),
            DefaultTransitTimeEntryRequiredLaneRule("default transit time entry needs this field"),
            DefaultTransitTimeEntryTitleLengthRule(120),
            DefaultTransitTimeEntryAllowedLaneRule(mutableListOf<String>("primary", "tiered")),
        ),
    )
}
