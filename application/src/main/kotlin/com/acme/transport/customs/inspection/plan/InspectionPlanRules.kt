package com.acme.transport.customs.inspection.plan

/** A rule a inspection plan must pass. Check returns its problems, or none. */
interface InspectionPlanRule {
    fun check(item: InspectionPlan): MutableList<String>
}

data class InspectionPlanDescriptionLengthRule(var maxLength: Int) : InspectionPlanRule {
    override fun check(item: InspectionPlan): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionPlanCodePrefixRule(var prefix: String) : InspectionPlanRule {
    override fun check(item: InspectionPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
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

data class InspectionPlanBatchSizeRangeRule(var min: Int, var max: Int) : InspectionPlanRule {
    override fun check(item: InspectionPlan): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
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

data class InspectionPlanRequiredLabelRule(var message: String) : InspectionPlanRule {
    override fun check(item: InspectionPlan): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
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
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
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
            InspectionPlanDescriptionLengthRule(1000),
            InspectionPlanCodePrefixRule("OPS"),
            InspectionPlanRequiredCodeRule("inspection plan needs an entry"),
            InspectionPlanBatchSizeRangeRule(32, 255),
            InspectionPlanCodeLengthRule(16),
            InspectionPlanRequiredLabelRule("inspection plan needs this field"),
        ),
    )
}
