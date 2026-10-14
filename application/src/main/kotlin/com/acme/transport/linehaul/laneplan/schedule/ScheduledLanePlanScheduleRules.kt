package com.acme.transport.linehaul.laneplan.schedule

/** A rule a scheduled lane plan schedule must pass. Check returns its problems, or none. */
interface ScheduledLanePlanScheduleRule {
    fun check(item: ScheduledLanePlanSchedule): MutableList<String>
}

data class ScheduledLanePlanScheduleRequiredCategoryRule(var message: String) : ScheduledLanePlanScheduleRule {
    override fun check(item: ScheduledLanePlanSchedule): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLanePlanScheduleMarginRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledLanePlanScheduleRule {
    override fun check(item: ScheduledLanePlanSchedule): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class ScheduledLanePlanScheduleCategoryLengthRule(var maxLength: Int) : ScheduledLanePlanScheduleRule {
    override fun check(item: ScheduledLanePlanSchedule): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLanePlanScheduleRequiredCodeRule(var message: String) : ScheduledLanePlanScheduleRule {
    override fun check(item: ScheduledLanePlanSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLanePlanScheduleLabelLengthRule(var maxLength: Int) : ScheduledLanePlanScheduleRule {
    override fun check(item: ScheduledLanePlanSchedule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLanePlanScheduleLocaleLengthRule(var maxLength: Int) : ScheduledLanePlanScheduleRule {
    override fun check(item: ScheduledLanePlanSchedule): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled lane plan schedule. */
data class ScheduledLanePlanScheduleValidator(var rules: MutableList<ScheduledLanePlanScheduleRule>) {
    fun validate(item: ScheduledLanePlanSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledLanePlanSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledLanePlanSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledLanePlanScheduleValidator(): ScheduledLanePlanScheduleValidator {
    return ScheduledLanePlanScheduleValidator(
        mutableListOf<ScheduledLanePlanScheduleRule>(
            ScheduledLanePlanScheduleRequiredCategoryRule("scheduled lane plan schedule needs this field"),
            ScheduledLanePlanScheduleMarginRangeRule(0.0, 2569.6),
            ScheduledLanePlanScheduleCategoryLengthRule(3600),
            ScheduledLanePlanScheduleRequiredCodeRule("scheduled lane plan schedule needs a value"),
            ScheduledLanePlanScheduleLabelLengthRule(255),
            ScheduledLanePlanScheduleLocaleLengthRule(1000),
        ),
    )
}
