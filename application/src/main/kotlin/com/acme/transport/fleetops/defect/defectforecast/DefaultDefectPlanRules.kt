package com.acme.transport.fleetops.defect.defectforecast

/** A rule a default defect plan must pass. Check returns its problems, or none. */
interface DefaultDefectPlanRule {
    fun check(item: DefaultDefectPlan): MutableList<String>
}

data class DefaultDefectPlanThresholdRangeRule(var min: Double, var max: Double) : DefaultDefectPlanRule {
    override fun check(item: DefaultDefectPlan): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDefectPlanTitleLengthRule(var maxLength: Int) : DefaultDefectPlanRule {
    override fun check(item: DefaultDefectPlan): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDefectPlanRequiredCodeRule(var message: String) : DefaultDefectPlanRule {
    override fun check(item: DefaultDefectPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultDefectPlanCodeLengthRule(var maxLength: Int) : DefaultDefectPlanRule {
    override fun check(item: DefaultDefectPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDefectPlanEtaMinutesRangeRule(var min: Int, var max: Int) : DefaultDefectPlanRule {
    override fun check(item: DefaultDefectPlan): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class DefaultDefectPlanPinnedGuardRule(var limit: Int) : DefaultDefectPlanRule {
    override fun check(item: DefaultDefectPlan): MutableList<String> {
        if (item.pinned && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default defect plan. */
data class DefaultDefectPlanValidator(var rules: MutableList<DefaultDefectPlanRule>) {
    fun validate(item: DefaultDefectPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultDefectPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultDefectPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultDefectPlanValidator(): DefaultDefectPlanValidator {
    return DefaultDefectPlanValidator(
        mutableListOf<DefaultDefectPlanRule>(
            DefaultDefectPlanThresholdRangeRule(5.9, 2408.1),
            DefaultDefectPlanTitleLengthRule(86400),
            DefaultDefectPlanRequiredCodeRule("default defect plan needs this field"),
            DefaultDefectPlanCodeLengthRule(86400),
            DefaultDefectPlanEtaMinutesRangeRule(32, 80),
            DefaultDefectPlanPinnedGuardRule(64),
        ),
    )
}
