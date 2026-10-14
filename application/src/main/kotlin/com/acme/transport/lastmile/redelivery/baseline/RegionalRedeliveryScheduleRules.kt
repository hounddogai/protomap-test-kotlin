package com.acme.transport.lastmile.redelivery.baseline

import com.acme.transport.kit.text.normalize

/** A rule a regional redelivery schedule must pass. Check returns its problems, or none. */
interface RegionalRedeliveryScheduleRule {
    fun check(item: RegionalRedeliverySchedule): MutableList<String>
}

data class RegionalRedeliveryScheduleLocaleLengthRule(var maxLength: Int) : RegionalRedeliveryScheduleRule {
    override fun check(item: RegionalRedeliverySchedule): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRedeliveryScheduleAllowedDescriptionRule(
    var allowed: MutableList<String>,
) : RegionalRedeliveryScheduleRule {
    override fun check(item: RegionalRedeliverySchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRedeliveryScheduleRetriesRangeRule(
    var min: Int,
    var max: Int,
) : RegionalRedeliveryScheduleRule {
    override fun check(item: RegionalRedeliverySchedule): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a regional redelivery schedule. */
data class RegionalRedeliveryScheduleValidator(var rules: MutableList<RegionalRedeliveryScheduleRule>) {
    fun validate(item: RegionalRedeliverySchedule): MutableList<String> {
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

    fun requireValid(item: RegionalRedeliverySchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalRedeliverySchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalRedeliveryScheduleValidator(): RegionalRedeliveryScheduleValidator {
    return RegionalRedeliveryScheduleValidator(
        mutableListOf<RegionalRedeliveryScheduleRule>(
            RegionalRedeliveryScheduleLocaleLengthRule(64),
            RegionalRedeliveryScheduleAllowedDescriptionRule(
                mutableListOf<String>("strict", "rolling", "manual"),
            ),
            RegionalRedeliveryScheduleRetriesRangeRule(16, 80),
        ),
    )
}
