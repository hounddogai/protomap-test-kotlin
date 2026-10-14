package com.acme.transport.terminal.traileryard.policy

/** A rule a regional trailer yard schedule must pass. Check returns its problems, or none. */
interface RegionalTrailerYardScheduleRule {
    fun check(item: RegionalTrailerYardSchedule): MutableList<String>
}

data class RegionalTrailerYardScheduleOwnerIdLengthRule(var maxLength: Int) : RegionalTrailerYardScheduleRule {
    override fun check(item: RegionalTrailerYardSchedule): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerYardScheduleRateRangeRule(
    var min: Double,
    var max: Double,
) : RegionalTrailerYardScheduleRule {
    override fun check(item: RegionalTrailerYardSchedule): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerYardScheduleLabelLengthRule(var maxLength: Int) : RegionalTrailerYardScheduleRule {
    override fun check(item: RegionalTrailerYardSchedule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalTrailerYardScheduleLimitRangeRule(
    var min: Int,
    var max: Int,
) : RegionalTrailerYardScheduleRule {
    override fun check(item: RegionalTrailerYardSchedule): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a regional trailer yard schedule. */
data class RegionalTrailerYardScheduleValidator(var rules: MutableList<RegionalTrailerYardScheduleRule>) {
    fun validate(item: RegionalTrailerYardSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalTrailerYardSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalTrailerYardSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalTrailerYardScheduleValidator(): RegionalTrailerYardScheduleValidator {
    return RegionalTrailerYardScheduleValidator(
        mutableListOf<RegionalTrailerYardScheduleRule>(
            RegionalTrailerYardScheduleOwnerIdLengthRule(32),
            RegionalTrailerYardScheduleRateRangeRule(2.9, 1811.1),
            RegionalTrailerYardScheduleLabelLengthRule(16),
            RegionalTrailerYardScheduleLimitRangeRule(32, 120),
        ),
    )
}
