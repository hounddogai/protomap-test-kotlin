package com.acme.transport.safety.incident.plan

import com.acme.transport.kit.text.normalize

/** A rule a tiered incident schedule must pass. Check returns its problems, or none. */
interface TieredIncidentScheduleRule {
    fun check(item: TieredIncidentSchedule): MutableList<String>
}

data class TieredIncidentScheduleRequiredContactEmailRule(var message: String) : TieredIncidentScheduleRule {
    override fun check(item: TieredIncidentSchedule): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredIncidentScheduleAllowedContactEmailRule(
    var allowed: MutableList<String>,
) : TieredIncidentScheduleRule {
    override fun check(item: TieredIncidentSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class TieredIncidentScheduleCapacityRangeRule(var min: Int, var max: Int) : TieredIncidentScheduleRule {
    override fun check(item: TieredIncidentSchedule): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredIncidentScheduleThresholdRangeRule(
    var min: Double,
    var max: Double,
) : TieredIncidentScheduleRule {
    override fun check(item: TieredIncidentSchedule): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered incident schedule. */
data class TieredIncidentScheduleValidator(var rules: MutableList<TieredIncidentScheduleRule>) {
    fun validate(item: TieredIncidentSchedule): MutableList<String> {
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

    fun requireValid(item: TieredIncidentSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredIncidentSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredIncidentScheduleValidator(): TieredIncidentScheduleValidator {
    return TieredIncidentScheduleValidator(
        mutableListOf<TieredIncidentScheduleRule>(
            TieredIncidentScheduleRequiredContactEmailRule("tiered incident schedule needs a value"),
            TieredIncidentScheduleAllowedContactEmailRule(mutableListOf<String>("strict", "default")),
            TieredIncidentScheduleCapacityRangeRule(255, 3600),
            TieredIncidentScheduleThresholdRangeRule(3.5, 3086.7),
        ),
    )
}
