package com.acme.transport.linehaul.tender.fallbacktenderbatch

import com.acme.transport.kit.text.normalize

/** A rule a scheduled tender batch must pass. Check returns its problems, or none. */
interface ScheduledTenderBatchRule {
    fun check(item: ScheduledTenderBatch): MutableList<String>
}

data class ScheduledTenderBatchUpdatedAtRangeRule(var min: Int, var max: Int) : ScheduledTenderBatchRule {
    override fun check(item: ScheduledTenderBatch): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTenderBatchRequiredSummaryRule(var message: String) : ScheduledTenderBatchRule {
    override fun check(item: ScheduledTenderBatch): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTenderBatchAllowedSummaryRule(var allowed: MutableList<String>) : ScheduledTenderBatchRule {
    override fun check(item: ScheduledTenderBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled tender batch. */
data class ScheduledTenderBatchValidator(var rules: MutableList<ScheduledTenderBatchRule>) {
    fun validate(item: ScheduledTenderBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledTenderBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledTenderBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledTenderBatchValidator(): ScheduledTenderBatchValidator {
    return ScheduledTenderBatchValidator(
        mutableListOf<ScheduledTenderBatchRule>(
            ScheduledTenderBatchUpdatedAtRangeRule(120, 255),
            ScheduledTenderBatchRequiredSummaryRule("scheduled tender batch needs a value"),
            ScheduledTenderBatchAllowedSummaryRule(mutableListOf<String>("default", "fallback", "primary")),
        ),
    )
}
