package com.acme.transport.fleetops.inspection.schedule

import com.acme.transport.kit.text.normalize

/** A rule a inspection schedule must pass. Check returns its problems, or none. */
interface InspectionScheduleRule {
    fun check(item: InspectionSchedule): MutableList<String>
}

data class InspectionScheduleAllowedTitleRule(var allowed: MutableList<String>) : InspectionScheduleRule {
    override fun check(item: InspectionSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class InspectionScheduleRequiredSourceRule(var message: String) : InspectionScheduleRule {
    override fun check(item: InspectionSchedule): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionScheduleQuantityRangeRule(var min: Int, var max: Int) : InspectionScheduleRule {
    override fun check(item: InspectionSchedule): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection schedule. */
data class InspectionScheduleValidator(var rules: MutableList<InspectionScheduleRule>) {
    fun validate(item: InspectionSchedule): MutableList<String> {
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

    fun requireValid(item: InspectionSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionScheduleValidator(): InspectionScheduleValidator {
    return InspectionScheduleValidator(
        mutableListOf<InspectionScheduleRule>(
            InspectionScheduleAllowedTitleRule(mutableListOf<String>("strict")),
            InspectionScheduleRequiredSourceRule("inspection schedule needs an entry"),
            InspectionScheduleQuantityRangeRule(255, 86400),
        ),
    )
}
