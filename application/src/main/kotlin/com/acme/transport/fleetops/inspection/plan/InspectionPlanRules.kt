package com.acme.transport.fleetops.inspection.plan

/** A rule a inspection plan must pass. Check returns its problems, or none. */
interface InspectionPlanRule {
    fun check(item: InspectionPlan): MutableList<String>
}

data class InspectionPlanStartsAtRangeRule(var min: Int, var max: Int) : InspectionPlanRule {
    override fun check(item: InspectionPlan): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class InspectionPlanMaximumRangeRule(var min: Double, var max: Double) : InspectionPlanRule {
    override fun check(item: InspectionPlan): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class InspectionPlanRequiredCodeRule(var message: String) : InspectionPlanRule {
    override fun check(item: InspectionPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionPlanCodeLengthRule(var maxLength: Int) : InspectionPlanRule {
    override fun check(item: InspectionPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection plan. */
data class InspectionPlanValidator(var rules: MutableList<InspectionPlanRule>) {
    fun validate(item: InspectionPlan): MutableList<String> {
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

    fun requireValid(item: InspectionPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionPlanValidator(): InspectionPlanValidator {
    return InspectionPlanValidator(
        mutableListOf<InspectionPlanRule>(
            InspectionPlanStartsAtRangeRule(16, 120),
            InspectionPlanMaximumRangeRule(1.8, 1667.4),
            InspectionPlanRequiredCodeRule("inspection plan needs a value"),
            InspectionPlanCodeLengthRule(255),
        ),
    )
}
