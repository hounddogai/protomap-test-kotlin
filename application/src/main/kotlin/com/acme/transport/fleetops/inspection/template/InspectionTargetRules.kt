package com.acme.transport.fleetops.inspection.template

/** A rule a inspection target must pass. Check returns its problems, or none. */
interface InspectionTargetRule {
    fun check(item: InspectionTarget): MutableList<String>
}

data class InspectionTargetOdometerRangeRule(var min: Int, var max: Int) : InspectionTargetRule {
    override fun check(item: InspectionTarget): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class InspectionTargetRequiredTitleRule(var message: String) : InspectionTargetRule {
    override fun check(item: InspectionTarget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionTargetRequiredCodeRule(var message: String) : InspectionTargetRule {
    override fun check(item: InspectionTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection target. */
data class InspectionTargetValidator(var rules: MutableList<InspectionTargetRule>) {
    fun validate(item: InspectionTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionTargetValidator(): InspectionTargetValidator {
    return InspectionTargetValidator(
        mutableListOf<InspectionTargetRule>(
            InspectionTargetOdometerRangeRule(16, 1000),
            InspectionTargetRequiredTitleRule("inspection target needs this field"),
            InspectionTargetRequiredCodeRule("inspection target needs a value"),
        ),
    )
}
