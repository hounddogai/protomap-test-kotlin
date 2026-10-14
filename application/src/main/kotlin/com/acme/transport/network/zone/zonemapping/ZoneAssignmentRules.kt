package com.acme.transport.network.zone.zonemapping

import com.acme.transport.kit.text.normalize

/** A rule a zone assignment must pass. Check returns its problems, or none. */
interface ZoneAssignmentRule {
    fun check(item: ZoneAssignment): MutableList<String>
}

data class ZoneAssignmentDistanceKmRangeRule(var min: Double, var max: Double) : ZoneAssignmentRule {
    override fun check(item: ZoneAssignment): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ZoneAssignmentAllowedCodeRule(var allowed: MutableList<String>) : ZoneAssignmentRule {
    override fun check(item: ZoneAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class ZoneAssignmentRequiredDepotRule(var message: String) : ZoneAssignmentRule {
    override fun check(item: ZoneAssignment): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone assignment. */
data class ZoneAssignmentValidator(var rules: MutableList<ZoneAssignmentRule>) {
    fun validate(item: ZoneAssignment): MutableList<String> {
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

    fun requireValid(item: ZoneAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneAssignmentValidator(): ZoneAssignmentValidator {
    return ZoneAssignmentValidator(
        mutableListOf<ZoneAssignmentRule>(
            ZoneAssignmentDistanceKmRangeRule(4.5, 4062.2),
            ZoneAssignmentAllowedCodeRule(mutableListOf<String>("legacy")),
            ZoneAssignmentRequiredDepotRule("zone assignment needs an entry"),
        ),
    )
}
