package com.acme.transport.fleetops.serviceplan.legacyplanquota

/** A rule a weighted service plan milestone must pass. Check returns its problems, or none. */
interface WeightedServicePlanMilestoneRule {
    fun check(item: WeightedServicePlanMilestone): MutableList<String>
}

data class WeightedServicePlanMilestoneRouteCodeLengthRule(
    var maxLength: Int,
) : WeightedServicePlanMilestoneRule {
    override fun check(item: WeightedServicePlanMilestone): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedServicePlanMilestoneRequiredLabelRule(
    var message: String,
) : WeightedServicePlanMilestoneRule {
    override fun check(item: WeightedServicePlanMilestone): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedServicePlanMilestoneFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : WeightedServicePlanMilestoneRule {
    override fun check(item: WeightedServicePlanMilestone): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class WeightedServicePlanMilestoneRequiredRouteCodeRule(
    var message: String,
) : WeightedServicePlanMilestoneRule {
    override fun check(item: WeightedServicePlanMilestone): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedServicePlanMilestoneLabelLengthRule(var maxLength: Int) : WeightedServicePlanMilestoneRule {
    override fun check(item: WeightedServicePlanMilestone): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedServicePlanMilestoneExternalRefLengthRule(
    var maxLength: Int,
) : WeightedServicePlanMilestoneRule {
    override fun check(item: WeightedServicePlanMilestone): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted service plan milestone. */
data class WeightedServicePlanMilestoneValidator(var rules: MutableList<WeightedServicePlanMilestoneRule>) {
    fun validate(item: WeightedServicePlanMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedServicePlanMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedServicePlanMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedServicePlanMilestoneValidator(): WeightedServicePlanMilestoneValidator {
    return WeightedServicePlanMilestoneValidator(
        mutableListOf<WeightedServicePlanMilestoneRule>(
            WeightedServicePlanMilestoneRouteCodeLengthRule(64),
            WeightedServicePlanMilestoneRequiredLabelRule("weighted service plan milestone needs this field"),
            WeightedServicePlanMilestoneFloorValueRangeRule(6.2, 1430.8),
            WeightedServicePlanMilestoneRequiredRouteCodeRule("weighted service plan milestone needs a value"),
            WeightedServicePlanMilestoneLabelLengthRule(255),
            WeightedServicePlanMilestoneExternalRefLengthRule(32),
        ),
    )
}
