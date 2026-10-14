package com.acme.transport.safety.coaching.entry

/** A rule a tiered coaching schedule must pass. Check returns its problems, or none. */
interface TieredCoachingScheduleRule {
    fun check(item: TieredCoachingSchedule): MutableList<String>
}

data class TieredCoachingScheduleRequiredCodeRule(var message: String) : TieredCoachingScheduleRule {
    override fun check(item: TieredCoachingSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCoachingScheduleRequiredLabelRule(var message: String) : TieredCoachingScheduleRule {
    override fun check(item: TieredCoachingSchedule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCoachingScheduleLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : TieredCoachingScheduleRule {
    override fun check(item: TieredCoachingSchedule): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tiered coaching schedule. */
data class TieredCoachingScheduleValidator(var rules: MutableList<TieredCoachingScheduleRule>) {
    fun validate(item: TieredCoachingSchedule): MutableList<String> {
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

    fun requireValid(item: TieredCoachingSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCoachingSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCoachingScheduleValidator(): TieredCoachingScheduleValidator {
    return TieredCoachingScheduleValidator(
        mutableListOf<TieredCoachingScheduleRule>(
            TieredCoachingScheduleRequiredCodeRule("tiered coaching schedule needs a value"),
            TieredCoachingScheduleRequiredLabelRule("tiered coaching schedule needs an entry"),
            TieredCoachingScheduleLoadKgRangeRule(9.4, 612.8),
        ),
    )
}
