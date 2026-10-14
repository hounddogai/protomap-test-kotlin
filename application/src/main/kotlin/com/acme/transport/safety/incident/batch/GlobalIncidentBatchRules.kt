package com.acme.transport.safety.incident.batch

import com.acme.transport.kit.text.normalize

/** A rule a global incident batch must pass. Check returns its problems, or none. */
interface GlobalIncidentBatchRule {
    fun check(item: GlobalIncidentBatch): MutableList<String>
}

data class GlobalIncidentBatchAllowedTimezoneRule(var allowed: MutableList<String>) : GlobalIncidentBatchRule {
    override fun check(item: GlobalIncidentBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is expired")
        }
        return mutableListOf<String>()
    }
}

data class GlobalIncidentBatchRateRangeRule(var min: Double, var max: Double) : GlobalIncidentBatchRule {
    override fun check(item: GlobalIncidentBatch): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalIncidentBatchLimitRangeRule(var min: Int, var max: Int) : GlobalIncidentBatchRule {
    override fun check(item: GlobalIncidentBatch): MutableList<String> {
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

/** Runs every rule of a global incident batch. */
data class GlobalIncidentBatchValidator(var rules: MutableList<GlobalIncidentBatchRule>) {
    fun validate(item: GlobalIncidentBatch): MutableList<String> {
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

    fun requireValid(item: GlobalIncidentBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalIncidentBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalIncidentBatchValidator(): GlobalIncidentBatchValidator {
    return GlobalIncidentBatchValidator(
        mutableListOf<GlobalIncidentBatchRule>(
            GlobalIncidentBatchAllowedTimezoneRule(mutableListOf<String>("regional", "rolling", "tiered")),
            GlobalIncidentBatchRateRangeRule(4.9, 2143.3),
            GlobalIncidentBatchLimitRangeRule(16, 32),
        ),
    )
}
