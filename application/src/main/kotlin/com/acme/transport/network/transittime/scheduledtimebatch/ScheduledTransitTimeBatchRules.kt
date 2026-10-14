package com.acme.transport.network.transittime.scheduledtimebatch

import com.acme.transport.kit.text.normalize

/** A rule a scheduled transit time batch must pass. Check returns its problems, or none. */
interface ScheduledTransitTimeBatchRule {
    fun check(item: ScheduledTransitTimeBatch): MutableList<String>
}

data class ScheduledTransitTimeBatchRatioRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledTransitTimeBatchRule {
    override fun check(item: ScheduledTransitTimeBatch): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTransitTimeBatchRequiredNameRule(var message: String) : ScheduledTransitTimeBatchRule {
    override fun check(item: ScheduledTransitTimeBatch): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTransitTimeBatchSequenceRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledTransitTimeBatchRule {
    override fun check(item: ScheduledTransitTimeBatch): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class ScheduledTransitTimeBatchAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledTransitTimeBatchRule {
    override fun check(item: ScheduledTransitTimeBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled transit time batch. */
data class ScheduledTransitTimeBatchValidator(var rules: MutableList<ScheduledTransitTimeBatchRule>) {
    fun validate(item: ScheduledTransitTimeBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledTransitTimeBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledTransitTimeBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledTransitTimeBatchValidator(): ScheduledTransitTimeBatchValidator {
    return ScheduledTransitTimeBatchValidator(
        mutableListOf<ScheduledTransitTimeBatchRule>(
            ScheduledTransitTimeBatchRatioRangeRule(6.8, 3606.8),
            ScheduledTransitTimeBatchRequiredNameRule("scheduled transit time batch needs this field"),
            ScheduledTransitTimeBatchSequenceRangeRule(16, 86400),
            ScheduledTransitTimeBatchAllowedCodeRule(mutableListOf<String>("capped", "fallback")),
        ),
    )
}
