package com.acme.transport.safety.scorecard.scorecardledger

/** A rule a tiered scorecard schedule must pass. Check returns its problems, or none. */
interface TieredScorecardScheduleRule {
    fun check(item: TieredScorecardSchedule): MutableList<String>
}

data class TieredScorecardScheduleCodePrefixRule(var prefix: String) : TieredScorecardScheduleRule {
    override fun check(item: TieredScorecardSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredScorecardScheduleDescriptionLengthRule(var maxLength: Int) : TieredScorecardScheduleRule {
    override fun check(item: TieredScorecardSchedule): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredScorecardScheduleRequiredVehicleIdRule(var message: String) : TieredScorecardScheduleRule {
    override fun check(item: TieredScorecardSchedule): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredScorecardScheduleTitleLengthRule(var maxLength: Int) : TieredScorecardScheduleRule {
    override fun check(item: TieredScorecardSchedule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredScorecardScheduleDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : TieredScorecardScheduleRule {
    override fun check(item: TieredScorecardSchedule): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tiered scorecard schedule. */
data class TieredScorecardScheduleValidator(var rules: MutableList<TieredScorecardScheduleRule>) {
    fun validate(item: TieredScorecardSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredScorecardSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredScorecardSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredScorecardScheduleValidator(): TieredScorecardScheduleValidator {
    return TieredScorecardScheduleValidator(
        mutableListOf<TieredScorecardScheduleRule>(
            TieredScorecardScheduleCodePrefixRule("REF"),
            TieredScorecardScheduleDescriptionLengthRule(64),
            TieredScorecardScheduleRequiredVehicleIdRule("tiered scorecard schedule needs this field"),
            TieredScorecardScheduleTitleLengthRule(255),
            TieredScorecardScheduleDurationMinutesRangeRule(64, 255),
        ),
    )
}
