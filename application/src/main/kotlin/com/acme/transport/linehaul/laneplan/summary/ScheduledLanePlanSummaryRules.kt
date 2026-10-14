package com.acme.transport.linehaul.laneplan.summary

import com.acme.transport.kit.text.normalize

/** A rule a scheduled lane plan summary must pass. Check returns its problems, or none. */
interface ScheduledLanePlanSummaryRule {
    fun check(item: ScheduledLanePlanSummary): MutableList<String>
}

data class ScheduledLanePlanSummaryAllowedSegmentRule(
    var allowed: MutableList<String>,
) : ScheduledLanePlanSummaryRule {
    override fun check(item: ScheduledLanePlanSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is stale")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLanePlanSummaryRequiredTitleRule(var message: String) : ScheduledLanePlanSummaryRule {
    override fun check(item: ScheduledLanePlanSummary): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLanePlanSummaryCodeLengthRule(var maxLength: Int) : ScheduledLanePlanSummaryRule {
    override fun check(item: ScheduledLanePlanSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled lane plan summary. */
data class ScheduledLanePlanSummaryValidator(var rules: MutableList<ScheduledLanePlanSummaryRule>) {
    fun validate(item: ScheduledLanePlanSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledLanePlanSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledLanePlanSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledLanePlanSummaryValidator(): ScheduledLanePlanSummaryValidator {
    return ScheduledLanePlanSummaryValidator(
        mutableListOf<ScheduledLanePlanSummaryRule>(
            ScheduledLanePlanSummaryAllowedSegmentRule(mutableListOf<String>("scheduled", "manual")),
            ScheduledLanePlanSummaryRequiredTitleRule("scheduled lane plan summary needs this field"),
            ScheduledLanePlanSummaryCodeLengthRule(80),
        ),
    )
}
