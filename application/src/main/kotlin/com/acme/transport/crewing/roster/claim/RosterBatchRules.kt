package com.acme.transport.crewing.roster.claim

import com.acme.transport.kit.text.normalize

/** A rule a roster batch must pass. Check returns its problems, or none. */
interface RosterBatchRule {
    fun check(item: RosterBatch): MutableList<String>
}

data class RosterBatchCarrierLengthRule(var maxLength: Int) : RosterBatchRule {
    override fun check(item: RosterBatch): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterBatchAllowedCarrierRule(var allowed: MutableList<String>) : RosterBatchRule {
    override fun check(item: RosterBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is expired")
        }
        return mutableListOf<String>()
    }
}

data class RosterBatchUpdatedAtRangeRule(var min: Int, var max: Int) : RosterBatchRule {
    override fun check(item: RosterBatch): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a roster batch. */
data class RosterBatchValidator(var rules: MutableList<RosterBatchRule>) {
    fun validate(item: RosterBatch): MutableList<String> {
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

    fun requireValid(item: RosterBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterBatchValidator(): RosterBatchValidator {
    return RosterBatchValidator(
        mutableListOf<RosterBatchRule>(
            RosterBatchCarrierLengthRule(120),
            RosterBatchAllowedCarrierRule(mutableListOf<String>("adaptive", "lenient")),
            RosterBatchUpdatedAtRangeRule(16, 120),
        ),
    )
}
