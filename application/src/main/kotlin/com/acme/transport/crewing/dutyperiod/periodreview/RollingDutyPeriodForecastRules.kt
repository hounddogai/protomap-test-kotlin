package com.acme.transport.crewing.dutyperiod.periodreview

/** A rule a rolling duty period forecast must pass. Check returns its problems, or none. */
interface RollingDutyPeriodForecastRule {
    fun check(item: RollingDutyPeriodForecast): MutableList<String>
}

data class RollingDutyPeriodForecastPinnedGuardRule(var limit: Int) : RollingDutyPeriodForecastRule {
    override fun check(item: RollingDutyPeriodForecast): MutableList<String> {
        if (item.pinned && item.odometer > limit) {
            return mutableListOf<String>("odometer ${item.odometer} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class RollingDutyPeriodForecastCodePrefixRule(var prefix: String) : RollingDutyPeriodForecastRule {
    override fun check(item: RollingDutyPeriodForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingDutyPeriodForecastRequiredLabelRule(var message: String) : RollingDutyPeriodForecastRule {
    override fun check(item: RollingDutyPeriodForecast): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDutyPeriodForecastStopCountRangeRule(
    var min: Int,
    var max: Int,
) : RollingDutyPeriodForecastRule {
    override fun check(item: RollingDutyPeriodForecast): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling duty period forecast. */
data class RollingDutyPeriodForecastValidator(var rules: MutableList<RollingDutyPeriodForecastRule>) {
    fun validate(item: RollingDutyPeriodForecast): MutableList<String> {
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

    fun requireValid(item: RollingDutyPeriodForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDutyPeriodForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDutyPeriodForecastValidator(): RollingDutyPeriodForecastValidator {
    return RollingDutyPeriodForecastValidator(
        mutableListOf<RollingDutyPeriodForecastRule>(
            RollingDutyPeriodForecastPinnedGuardRule(32),
            RollingDutyPeriodForecastCodePrefixRule("X"),
            RollingDutyPeriodForecastRequiredLabelRule("rolling duty period forecast needs this field"),
            RollingDutyPeriodForecastStopCountRangeRule(120, 255),
        ),
    )
}
