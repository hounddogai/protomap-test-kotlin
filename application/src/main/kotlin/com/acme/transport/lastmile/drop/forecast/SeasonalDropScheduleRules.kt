package com.acme.transport.lastmile.drop.forecast

/** A rule a seasonal drop schedule must pass. Check returns its problems, or none. */
interface SeasonalDropScheduleRule {
    fun check(item: SeasonalDropSchedule): MutableList<String>
}

data class SeasonalDropScheduleDescriptionLengthRule(var maxLength: Int) : SeasonalDropScheduleRule {
    override fun check(item: SeasonalDropSchedule): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDropScheduleCodePrefixRule(var prefix: String) : SeasonalDropScheduleRule {
    override fun check(item: SeasonalDropSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDropScheduleRequiredCodeRule(var message: String) : SeasonalDropScheduleRule {
    override fun check(item: SeasonalDropSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDropScheduleCodeLengthRule(var maxLength: Int) : SeasonalDropScheduleRule {
    override fun check(item: SeasonalDropSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDropScheduleRequiredContactEmailRule(var message: String) : SeasonalDropScheduleRule {
    override fun check(item: SeasonalDropSchedule): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDropScheduleDurationMinutesRangeRule(var min: Int, var max: Int) : SeasonalDropScheduleRule {
    override fun check(item: SeasonalDropSchedule): MutableList<String> {
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

/** Runs every rule of a seasonal drop schedule. */
data class SeasonalDropScheduleValidator(var rules: MutableList<SeasonalDropScheduleRule>) {
    fun validate(item: SeasonalDropSchedule): MutableList<String> {
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

    fun requireValid(item: SeasonalDropSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDropSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDropScheduleValidator(): SeasonalDropScheduleValidator {
    return SeasonalDropScheduleValidator(
        mutableListOf<SeasonalDropScheduleRule>(
            SeasonalDropScheduleDescriptionLengthRule(16),
            SeasonalDropScheduleCodePrefixRule("OPS"),
            SeasonalDropScheduleRequiredCodeRule("seasonal drop schedule needs a value"),
            SeasonalDropScheduleCodeLengthRule(80),
            SeasonalDropScheduleRequiredContactEmailRule("seasonal drop schedule needs an entry"),
            SeasonalDropScheduleDurationMinutesRangeRule(255, 255),
        ),
    )
}
