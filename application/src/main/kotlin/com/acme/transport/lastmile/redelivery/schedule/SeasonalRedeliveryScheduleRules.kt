package com.acme.transport.lastmile.redelivery.schedule

import com.acme.transport.kit.text.normalize

/** A rule a seasonal redelivery schedule must pass. Check returns its problems, or none. */
interface SeasonalRedeliveryScheduleRule {
    fun check(item: SeasonalRedeliverySchedule): MutableList<String>
}

data class SeasonalRedeliveryScheduleRequiredCodeRule(var message: String) : SeasonalRedeliveryScheduleRule {
    override fun check(item: SeasonalRedeliverySchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRedeliveryScheduleAllowedCodeRule(
    var allowed: MutableList<String>,
) : SeasonalRedeliveryScheduleRule {
    override fun check(item: SeasonalRedeliverySchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRedeliveryScheduleCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalRedeliveryScheduleRule {
    override fun check(item: SeasonalRedeliverySchedule): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class SeasonalRedeliveryScheduleRatioRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalRedeliveryScheduleRule {
    override fun check(item: SeasonalRedeliverySchedule): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal redelivery schedule. */
data class SeasonalRedeliveryScheduleValidator(var rules: MutableList<SeasonalRedeliveryScheduleRule>) {
    fun validate(item: SeasonalRedeliverySchedule): MutableList<String> {
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

    fun requireValid(item: SeasonalRedeliverySchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRedeliverySchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRedeliveryScheduleValidator(): SeasonalRedeliveryScheduleValidator {
    return SeasonalRedeliveryScheduleValidator(
        mutableListOf<SeasonalRedeliveryScheduleRule>(
            SeasonalRedeliveryScheduleRequiredCodeRule("seasonal redelivery schedule needs this field"),
            SeasonalRedeliveryScheduleAllowedCodeRule(mutableListOf<String>("weighted", "default", "weighted")),
            SeasonalRedeliveryScheduleCreatedAtRangeRule(255, 86400),
            SeasonalRedeliveryScheduleRatioRangeRule(4.0, 2146.8),
        ),
    )
}
