package com.acme.transport.fleetops.defect.defectcheckpoint

/** A rule a adaptive defect plan must pass. Check returns its problems, or none. */
interface AdaptiveDefectPlanRule {
    fun check(item: AdaptiveDefectPlan): MutableList<String>
}

data class AdaptiveDefectPlanRequiredNameRule(var message: String) : AdaptiveDefectPlanRule {
    override fun check(item: AdaptiveDefectPlan): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDefectPlanCodeLengthRule(var maxLength: Int) : AdaptiveDefectPlanRule {
    override fun check(item: AdaptiveDefectPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDefectPlanBatchSizeRangeRule(var min: Int, var max: Int) : AdaptiveDefectPlanRule {
    override fun check(item: AdaptiveDefectPlan): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a adaptive defect plan. */
data class AdaptiveDefectPlanValidator(var rules: MutableList<AdaptiveDefectPlanRule>) {
    fun validate(item: AdaptiveDefectPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveDefectPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveDefectPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveDefectPlanValidator(): AdaptiveDefectPlanValidator {
    return AdaptiveDefectPlanValidator(
        mutableListOf<AdaptiveDefectPlanRule>(
            AdaptiveDefectPlanRequiredNameRule("adaptive defect plan needs this field"),
            AdaptiveDefectPlanCodeLengthRule(32),
            AdaptiveDefectPlanBatchSizeRangeRule(86400, 86400),
        ),
    )
}
