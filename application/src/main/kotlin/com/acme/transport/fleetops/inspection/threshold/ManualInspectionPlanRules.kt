package com.acme.transport.fleetops.inspection.threshold

/** A rule a manual inspection plan must pass. Check returns its problems, or none. */
interface ManualInspectionPlanRule {
    fun check(item: ManualInspectionPlan): MutableList<String>
}

data class ManualInspectionPlanCodePrefixRule(var prefix: String) : ManualInspectionPlanRule {
    override fun check(item: ManualInspectionPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionPlanRequiredCodeRule(var message: String) : ManualInspectionPlanRule {
    override fun check(item: ManualInspectionPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionPlanLabelLengthRule(var maxLength: Int) : ManualInspectionPlanRule {
    override fun check(item: ManualInspectionPlan): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionPlanCategoryLengthRule(var maxLength: Int) : ManualInspectionPlanRule {
    override fun check(item: ManualInspectionPlan): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionPlanScoreRangeRule(var min: Double, var max: Double) : ManualInspectionPlanRule {
    override fun check(item: ManualInspectionPlan): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class ManualInspectionPlanPinnedGuardRule(var limit: Double) : ManualInspectionPlanRule {
    override fun check(item: ManualInspectionPlan): MutableList<String> {
        if (item.pinned && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual inspection plan. */
data class ManualInspectionPlanValidator(var rules: MutableList<ManualInspectionPlanRule>) {
    fun validate(item: ManualInspectionPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualInspectionPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualInspectionPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultManualInspectionPlanValidator(): ManualInspectionPlanValidator {
    return ManualInspectionPlanValidator(
        mutableListOf<ManualInspectionPlanRule>(
            ManualInspectionPlanCodePrefixRule("OPS"),
            ManualInspectionPlanRequiredCodeRule("manual inspection plan needs a value"),
            ManualInspectionPlanLabelLengthRule(1000),
            ManualInspectionPlanCategoryLengthRule(1000),
            ManualInspectionPlanScoreRangeRule(8.6, 3604.4),
            ManualInspectionPlanPinnedGuardRule(7.7),
        ),
    )
}
