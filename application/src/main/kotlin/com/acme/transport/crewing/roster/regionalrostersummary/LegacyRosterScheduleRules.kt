package com.acme.transport.crewing.roster.regionalrostersummary

/** A rule a legacy roster schedule must pass. Check returns its problems, or none. */
interface LegacyRosterScheduleRule {
    fun check(item: LegacyRosterSchedule): MutableList<String>
}

data class LegacyRosterScheduleNameLengthRule(var maxLength: Int) : LegacyRosterScheduleRule {
    override fun check(item: LegacyRosterSchedule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRosterScheduleRatioRangeRule(var min: Double, var max: Double) : LegacyRosterScheduleRule {
    override fun check(item: LegacyRosterSchedule): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class LegacyRosterScheduleCodeLengthRule(var maxLength: Int) : LegacyRosterScheduleRule {
    override fun check(item: LegacyRosterSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRosterScheduleCreatedAtRangeRule(var min: Int, var max: Int) : LegacyRosterScheduleRule {
    override fun check(item: LegacyRosterSchedule): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy roster schedule. */
data class LegacyRosterScheduleValidator(var rules: MutableList<LegacyRosterScheduleRule>) {
    fun validate(item: LegacyRosterSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyRosterSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyRosterSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyRosterScheduleValidator(): LegacyRosterScheduleValidator {
    return LegacyRosterScheduleValidator(
        mutableListOf<LegacyRosterScheduleRule>(
            LegacyRosterScheduleNameLengthRule(32),
            LegacyRosterScheduleRatioRangeRule(6.3, 2339.9),
            LegacyRosterScheduleCodeLengthRule(1000),
            LegacyRosterScheduleCreatedAtRangeRule(120, 255),
        ),
    )
}
