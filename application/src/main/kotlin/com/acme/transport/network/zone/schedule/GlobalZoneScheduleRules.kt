package com.acme.transport.network.zone.schedule

/** A rule a global zone schedule must pass. Check returns its problems, or none. */
interface GlobalZoneScheduleRule {
    fun check(item: GlobalZoneSchedule): MutableList<String>
}

data class GlobalZoneScheduleWeightRangeRule(var min: Double, var max: Double) : GlobalZoneScheduleRule {
    override fun check(item: GlobalZoneSchedule): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class GlobalZoneScheduleTitleLengthRule(var maxLength: Int) : GlobalZoneScheduleRule {
    override fun check(item: GlobalZoneSchedule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalZoneScheduleRequiredCodeRule(var message: String) : GlobalZoneScheduleRule {
    override fun check(item: GlobalZoneSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalZoneScheduleRequiredCategoryRule(var message: String) : GlobalZoneScheduleRule {
    override fun check(item: GlobalZoneSchedule): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global zone schedule. */
data class GlobalZoneScheduleValidator(var rules: MutableList<GlobalZoneScheduleRule>) {
    fun validate(item: GlobalZoneSchedule): MutableList<String> {
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

    fun requireValid(item: GlobalZoneSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalZoneSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalZoneScheduleValidator(): GlobalZoneScheduleValidator {
    return GlobalZoneScheduleValidator(
        mutableListOf<GlobalZoneScheduleRule>(
            GlobalZoneScheduleWeightRangeRule(4.6, 4918.0),
            GlobalZoneScheduleTitleLengthRule(255),
            GlobalZoneScheduleRequiredCodeRule("global zone schedule needs this field"),
            GlobalZoneScheduleRequiredCategoryRule("global zone schedule needs a value"),
        ),
    )
}
