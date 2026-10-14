package com.acme.transport.fleetops.inspection.rollup

/** A rule a weighted inspection plan must pass. Check returns its problems, or none. */
interface WeightedInspectionPlanRule {
    fun check(item: WeightedInspectionPlan): MutableList<String>
}

data class WeightedInspectionPlanCostRangeRule(var min: Double, var max: Double) : WeightedInspectionPlanRule {
    override fun check(item: WeightedInspectionPlan): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionPlanRouteCodeLengthRule(var maxLength: Int) : WeightedInspectionPlanRule {
    override fun check(item: WeightedInspectionPlan): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionPlanFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : WeightedInspectionPlanRule {
    override fun check(item: WeightedInspectionPlan): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted inspection plan. */
data class WeightedInspectionPlanValidator(var rules: MutableList<WeightedInspectionPlanRule>) {
    fun validate(item: WeightedInspectionPlan): MutableList<String> {
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

    fun requireValid(item: WeightedInspectionPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedInspectionPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedInspectionPlanValidator(): WeightedInspectionPlanValidator {
    return WeightedInspectionPlanValidator(
        mutableListOf<WeightedInspectionPlanRule>(
            WeightedInspectionPlanCostRangeRule(8.4, 1190.0),
            WeightedInspectionPlanRouteCodeLengthRule(1000),
            WeightedInspectionPlanFloorValueRangeRule(2.9, 2143.5),
        ),
    )
}
