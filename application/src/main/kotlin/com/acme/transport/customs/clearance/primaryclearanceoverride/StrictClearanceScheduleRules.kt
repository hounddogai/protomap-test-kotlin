package com.acme.transport.customs.clearance.primaryclearanceoverride

/** A rule a strict clearance schedule must pass. Check returns its problems, or none. */
interface StrictClearanceScheduleRule {
    fun check(item: StrictClearanceSchedule): MutableList<String>
}

data class StrictClearanceScheduleRouteCodeLengthRule(var maxLength: Int) : StrictClearanceScheduleRule {
    override fun check(item: StrictClearanceSchedule): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictClearanceScheduleRequiredRouteCodeRule(var message: String) : StrictClearanceScheduleRule {
    override fun check(item: StrictClearanceSchedule): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictClearanceScheduleRequiredSummaryRule(var message: String) : StrictClearanceScheduleRule {
    override fun check(item: StrictClearanceSchedule): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictClearanceScheduleRequiredCodeRule(var message: String) : StrictClearanceScheduleRule {
    override fun check(item: StrictClearanceSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict clearance schedule. */
data class StrictClearanceScheduleValidator(var rules: MutableList<StrictClearanceScheduleRule>) {
    fun validate(item: StrictClearanceSchedule): MutableList<String> {
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

    fun requireValid(item: StrictClearanceSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictClearanceSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictClearanceScheduleValidator(): StrictClearanceScheduleValidator {
    return StrictClearanceScheduleValidator(
        mutableListOf<StrictClearanceScheduleRule>(
            StrictClearanceScheduleRouteCodeLengthRule(120),
            StrictClearanceScheduleRequiredRouteCodeRule("strict clearance schedule needs this field"),
            StrictClearanceScheduleRequiredSummaryRule("strict clearance schedule needs an entry"),
            StrictClearanceScheduleRequiredCodeRule("strict clearance schedule needs an entry"),
        ),
    )
}
