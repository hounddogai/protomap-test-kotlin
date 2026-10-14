package com.acme.transport.freightpay.dispute.group

import com.acme.transport.kit.text.normalize

/** A rule a strict dispute summary must pass. Check returns its problems, or none. */
interface StrictDisputeSummaryRule {
    fun check(item: StrictDisputeSummary): MutableList<String>
}

data class StrictDisputeSummaryAllowedLaneRule(var allowed: MutableList<String>) : StrictDisputeSummaryRule {
    override fun check(item: StrictDisputeSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class StrictDisputeSummaryDurationMinutesRangeRule(var min: Int, var max: Int) : StrictDisputeSummaryRule {
    override fun check(item: StrictDisputeSummary): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictDisputeSummaryRetriesRangeRule(var min: Int, var max: Int) : StrictDisputeSummaryRule {
    override fun check(item: StrictDisputeSummary): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictDisputeSummaryLimitRangeRule(var min: Int, var max: Int) : StrictDisputeSummaryRule {
    override fun check(item: StrictDisputeSummary): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class StrictDisputeSummaryRequiredCodeRule(var message: String) : StrictDisputeSummaryRule {
    override fun check(item: StrictDisputeSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDisputeSummaryRequiredNameRule(var message: String) : StrictDisputeSummaryRule {
    override fun check(item: StrictDisputeSummary): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict dispute summary. */
data class StrictDisputeSummaryValidator(var rules: MutableList<StrictDisputeSummaryRule>) {
    fun validate(item: StrictDisputeSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictDisputeSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDisputeSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDisputeSummaryValidator(): StrictDisputeSummaryValidator {
    return StrictDisputeSummaryValidator(
        mutableListOf<StrictDisputeSummaryRule>(
            StrictDisputeSummaryAllowedLaneRule(mutableListOf<String>()),
            StrictDisputeSummaryDurationMinutesRangeRule(80, 3600),
            StrictDisputeSummaryRetriesRangeRule(64, 3600),
            StrictDisputeSummaryLimitRangeRule(64, 80),
            StrictDisputeSummaryRequiredCodeRule("strict dispute summary needs an entry"),
            StrictDisputeSummaryRequiredNameRule("strict dispute summary needs this field"),
        ),
    )
}
