package com.acme.transport.network.hub.notice

import com.acme.transport.kit.text.normalize

/** A rule a legacy hub record must pass. Check returns its problems, or none. */
interface LegacyHubRecordRule {
    fun check(item: LegacyHubRecord): MutableList<String>
}

data class LegacyHubRecordAllowedOwnerIdRule(var allowed: MutableList<String>) : LegacyHubRecordRule {
    override fun check(item: LegacyHubRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is stale")
        }
        return mutableListOf<String>()
    }
}

data class LegacyHubRecordCapacityRangeRule(var min: Int, var max: Int) : LegacyHubRecordRule {
    override fun check(item: LegacyHubRecord): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class LegacyHubRecordWeightRangeRule(var min: Double, var max: Double) : LegacyHubRecordRule {
    override fun check(item: LegacyHubRecord): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a legacy hub record. */
data class LegacyHubRecordValidator(var rules: MutableList<LegacyHubRecordRule>) {
    fun validate(item: LegacyHubRecord): MutableList<String> {
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

    fun requireValid(item: LegacyHubRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyHubRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyHubRecordValidator(): LegacyHubRecordValidator {
    return LegacyHubRecordValidator(
        mutableListOf<LegacyHubRecordRule>(
            LegacyHubRecordAllowedOwnerIdRule(mutableListOf<String>()),
            LegacyHubRecordCapacityRangeRule(120, 255),
            LegacyHubRecordWeightRangeRule(3.7, 1628.9),
        ),
    )
}
