package com.acme.transport.crewing.dutyperiod.review

/** A rule a seasonal duty period schedule must pass. Check returns its problems, or none. */
interface SeasonalDutyPeriodScheduleRule {
    fun check(item: SeasonalDutyPeriodSchedule): MutableList<String>
}

data class SeasonalDutyPeriodScheduleCodeLengthRule(var maxLength: Int) : SeasonalDutyPeriodScheduleRule {
    override fun check(item: SeasonalDutyPeriodSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDutyPeriodScheduleDriverIdLengthRule(var maxLength: Int) : SeasonalDutyPeriodScheduleRule {
    override fun check(item: SeasonalDutyPeriodSchedule): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDutyPeriodScheduleRequiredDriverIdRule(
    var message: String,
) : SeasonalDutyPeriodScheduleRule {
    override fun check(item: SeasonalDutyPeriodSchedule): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDutyPeriodScheduleRequiredCarrierRule(var message: String) : SeasonalDutyPeriodScheduleRule {
    override fun check(item: SeasonalDutyPeriodSchedule): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal duty period schedule. */
data class SeasonalDutyPeriodScheduleValidator(var rules: MutableList<SeasonalDutyPeriodScheduleRule>) {
    fun validate(item: SeasonalDutyPeriodSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalDutyPeriodSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDutyPeriodSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDutyPeriodScheduleValidator(): SeasonalDutyPeriodScheduleValidator {
    return SeasonalDutyPeriodScheduleValidator(
        mutableListOf<SeasonalDutyPeriodScheduleRule>(
            SeasonalDutyPeriodScheduleCodeLengthRule(3600),
            SeasonalDutyPeriodScheduleDriverIdLengthRule(120),
            SeasonalDutyPeriodScheduleRequiredDriverIdRule("seasonal duty period schedule needs a value"),
            SeasonalDutyPeriodScheduleRequiredCarrierRule("seasonal duty period schedule needs this field"),
        ),
    )
}
