package com.acme.transport.safety.scorecard.schedule

import com.acme.transport.kit.text.normalize

/** A rule a scorecard schedule must pass. Check returns its problems, or none. */
interface ScorecardScheduleRule {
    fun check(item: ScorecardSchedule): MutableList<String>
}

data class ScorecardScheduleLimitRangeRule(var min: Int, var max: Int) : ScorecardScheduleRule {
    override fun check(item: ScorecardSchedule): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardScheduleAllowedVehicleIdRule(var allowed: MutableList<String>) : ScorecardScheduleRule {
    override fun check(item: ScorecardSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is stale")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardScheduleRequiredCodeRule(var message: String) : ScorecardScheduleRule {
    override fun check(item: ScorecardSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardScheduleQuantityRangeRule(var min: Int, var max: Int) : ScorecardScheduleRule {
    override fun check(item: ScorecardSchedule): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard schedule. */
data class ScorecardScheduleValidator(var rules: MutableList<ScorecardScheduleRule>) {
    fun validate(item: ScorecardSchedule): MutableList<String> {
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

    fun requireValid(item: ScorecardSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardScheduleValidator(): ScorecardScheduleValidator {
    return ScorecardScheduleValidator(
        mutableListOf<ScorecardScheduleRule>(
            ScorecardScheduleLimitRangeRule(80, 86400),
            ScorecardScheduleAllowedVehicleIdRule(mutableListOf<String>()),
            ScorecardScheduleRequiredCodeRule("scorecard schedule needs a value"),
            ScorecardScheduleQuantityRangeRule(80, 1000),
        ),
    )
}
