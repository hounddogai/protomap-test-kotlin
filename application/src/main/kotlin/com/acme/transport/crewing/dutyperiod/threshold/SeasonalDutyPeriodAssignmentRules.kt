package com.acme.transport.crewing.dutyperiod.threshold

/** A rule a seasonal duty period assignment must pass. Check returns its problems, or none. */
interface SeasonalDutyPeriodAssignmentRule {
    fun check(item: SeasonalDutyPeriodAssignment): MutableList<String>
}

data class SeasonalDutyPeriodAssignmentRequiredNameRule(
    var message: String,
) : SeasonalDutyPeriodAssignmentRule {
    override fun check(item: SeasonalDutyPeriodAssignment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDutyPeriodAssignmentEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalDutyPeriodAssignmentRule {
    override fun check(item: SeasonalDutyPeriodAssignment): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class SeasonalDutyPeriodAssignmentRequiredCodeRule(
    var message: String,
) : SeasonalDutyPeriodAssignmentRule {
    override fun check(item: SeasonalDutyPeriodAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal duty period assignment. */
data class SeasonalDutyPeriodAssignmentValidator(var rules: MutableList<SeasonalDutyPeriodAssignmentRule>) {
    fun validate(item: SeasonalDutyPeriodAssignment): MutableList<String> {
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

    fun requireValid(item: SeasonalDutyPeriodAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDutyPeriodAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDutyPeriodAssignmentValidator(): SeasonalDutyPeriodAssignmentValidator {
    return SeasonalDutyPeriodAssignmentValidator(
        mutableListOf<SeasonalDutyPeriodAssignmentRule>(
            SeasonalDutyPeriodAssignmentRequiredNameRule("seasonal duty period assignment needs this field"),
            SeasonalDutyPeriodAssignmentEtaMinutesRangeRule(80, 255),
            SeasonalDutyPeriodAssignmentRequiredCodeRule("seasonal duty period assignment needs a value"),
        ),
    )
}
