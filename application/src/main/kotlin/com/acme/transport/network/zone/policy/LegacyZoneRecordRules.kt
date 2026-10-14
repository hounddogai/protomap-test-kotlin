package com.acme.transport.network.zone.policy

import com.acme.transport.kit.text.normalize

/** A rule a legacy zone record must pass. Check returns its problems, or none. */
interface LegacyZoneRecordRule {
    fun check(item: LegacyZoneRecord): MutableList<String>
}

data class LegacyZoneRecordRequiredRouteCodeRule(var message: String) : LegacyZoneRecordRule {
    override fun check(item: LegacyZoneRecord): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyZoneRecordRequiredCodeRule(var message: String) : LegacyZoneRecordRule {
    override fun check(item: LegacyZoneRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyZoneRecordFuelLitersRangeRule(var min: Double, var max: Double) : LegacyZoneRecordRule {
    override fun check(item: LegacyZoneRecord): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyZoneRecordTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : LegacyZoneRecordRule {
    override fun check(item: LegacyZoneRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class LegacyZoneRecordAllowedRouteCodeRule(var allowed: MutableList<String>) : LegacyZoneRecordRule {
    override fun check(item: LegacyZoneRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy zone record. */
data class LegacyZoneRecordValidator(var rules: MutableList<LegacyZoneRecordRule>) {
    fun validate(item: LegacyZoneRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyZoneRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyZoneRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyZoneRecordValidator(): LegacyZoneRecordValidator {
    return LegacyZoneRecordValidator(
        mutableListOf<LegacyZoneRecordRule>(
            LegacyZoneRecordRequiredRouteCodeRule("legacy zone record needs an entry"),
            LegacyZoneRecordRequiredCodeRule("legacy zone record needs an entry"),
            LegacyZoneRecordFuelLitersRangeRule(1.6, 527.1),
            LegacyZoneRecordTagsLimitRule(16, mutableListOf<String>("manual", "scheduled")),
            LegacyZoneRecordAllowedRouteCodeRule(mutableListOf<String>()),
        ),
    )
}
