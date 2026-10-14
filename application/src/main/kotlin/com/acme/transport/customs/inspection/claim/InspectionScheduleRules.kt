package com.acme.transport.customs.inspection.claim

/** A rule a inspection schedule must pass. Check returns its problems, or none. */
interface InspectionScheduleRule {
    fun check(item: InspectionSchedule): MutableList<String>
}

data class InspectionScheduleRequiredDepotRule(var message: String) : InspectionScheduleRule {
    override fun check(item: InspectionSchedule): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionScheduleWeightRangeRule(var min: Double, var max: Double) : InspectionScheduleRule {
    override fun check(item: InspectionSchedule): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionScheduleLabelLengthRule(var maxLength: Int) : InspectionScheduleRule {
    override fun check(item: InspectionSchedule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection schedule. */
data class InspectionScheduleValidator(var rules: MutableList<InspectionScheduleRule>) {
    fun validate(item: InspectionSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
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
            InspectionScheduleRequiredDepotRule("inspection schedule needs this field"),
            InspectionScheduleWeightRangeRule(4.0, 1596.0),
            InspectionScheduleLabelLengthRule(3600),
        ),
    )
}
