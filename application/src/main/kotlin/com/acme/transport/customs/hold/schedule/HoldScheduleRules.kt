package com.acme.transport.customs.hold.schedule

/** A rule a hold schedule must pass. Check returns its problems, or none. */
interface HoldScheduleRule {
    fun check(item: HoldSchedule): MutableList<String>
}

data class HoldScheduleTitleLengthRule(var maxLength: Int) : HoldScheduleRule {
    override fun check(item: HoldSchedule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldScheduleRequiredContactEmailRule(var message: String) : HoldScheduleRule {
    override fun check(item: HoldSchedule): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldSchedulePriorityRangeRule(var min: Int, var max: Int) : HoldScheduleRule {
    override fun check(item: HoldSchedule): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class HoldScheduleRequiredCodeRule(var message: String) : HoldScheduleRule {
    override fun check(item: HoldSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldScheduleTrailerIdLengthRule(var maxLength: Int) : HoldScheduleRule {
    override fun check(item: HoldSchedule): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldScheduleContactEmailLengthRule(var maxLength: Int) : HoldScheduleRule {
    override fun check(item: HoldSchedule): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold schedule. */
data class HoldScheduleValidator(var rules: MutableList<HoldScheduleRule>) {
    fun validate(item: HoldSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HoldSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldScheduleValidator(): HoldScheduleValidator {
    return HoldScheduleValidator(
        mutableListOf<HoldScheduleRule>(
            HoldScheduleTitleLengthRule(86400),
            HoldScheduleRequiredContactEmailRule("hold schedule needs a value"),
            HoldSchedulePriorityRangeRule(64, 1000),
            HoldScheduleRequiredCodeRule("hold schedule needs a value"),
            HoldScheduleTrailerIdLengthRule(64),
            HoldScheduleContactEmailLengthRule(3600),
        ),
    )
}
