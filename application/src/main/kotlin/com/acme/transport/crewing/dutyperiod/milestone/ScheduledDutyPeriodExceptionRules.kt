package com.acme.transport.crewing.dutyperiod.milestone

import com.acme.transport.kit.text.normalize

/** A rule a scheduled duty period exception must pass. Check returns its problems, or none. */
interface ScheduledDutyPeriodExceptionRule {
    fun check(item: ScheduledDutyPeriodException): MutableList<String>
}

data class ScheduledDutyPeriodExceptionRequiredNameRule(
    var message: String,
) : ScheduledDutyPeriodExceptionRule {
    override fun check(item: ScheduledDutyPeriodException): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDutyPeriodExceptionRequiredRegionRule(
    var message: String,
) : ScheduledDutyPeriodExceptionRule {
    override fun check(item: ScheduledDutyPeriodException): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDutyPeriodExceptionAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledDutyPeriodExceptionRule {
    override fun check(item: ScheduledDutyPeriodException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled duty period exception. */
data class ScheduledDutyPeriodExceptionValidator(var rules: MutableList<ScheduledDutyPeriodExceptionRule>) {
    fun validate(item: ScheduledDutyPeriodException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledDutyPeriodException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDutyPeriodException>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDutyPeriodExceptionValidator(): ScheduledDutyPeriodExceptionValidator {
    return ScheduledDutyPeriodExceptionValidator(
        mutableListOf<ScheduledDutyPeriodExceptionRule>(
            ScheduledDutyPeriodExceptionRequiredNameRule("scheduled duty period exception needs a value"),
            ScheduledDutyPeriodExceptionRequiredRegionRule("scheduled duty period exception needs this field"),
            ScheduledDutyPeriodExceptionAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
