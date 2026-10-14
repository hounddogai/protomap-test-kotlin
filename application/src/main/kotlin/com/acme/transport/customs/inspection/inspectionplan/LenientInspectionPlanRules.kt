package com.acme.transport.customs.inspection.inspectionplan

/** A rule a lenient inspection plan must pass. Check returns its problems, or none. */
interface LenientInspectionPlanRule {
    fun check(item: LenientInspectionPlan): MutableList<String>
}

data class LenientInspectionPlanCarrierLengthRule(var maxLength: Int) : LenientInspectionPlanRule {
    override fun check(item: LenientInspectionPlan): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientInspectionPlanCodeLengthRule(var maxLength: Int) : LenientInspectionPlanRule {
    override fun check(item: LenientInspectionPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientInspectionPlanRequiredTitleRule(var message: String) : LenientInspectionPlanRule {
    override fun check(item: LenientInspectionPlan): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientInspectionPlanTitleLengthRule(var maxLength: Int) : LenientInspectionPlanRule {
    override fun check(item: LenientInspectionPlan): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientInspectionPlanMarginRangeRule(var min: Double, var max: Double) : LenientInspectionPlanRule {
    override fun check(item: LenientInspectionPlan): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient inspection plan. */
data class LenientInspectionPlanValidator(var rules: MutableList<LenientInspectionPlanRule>) {
    fun validate(item: LenientInspectionPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientInspectionPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientInspectionPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientInspectionPlanValidator(): LenientInspectionPlanValidator {
    return LenientInspectionPlanValidator(
        mutableListOf<LenientInspectionPlanRule>(
            LenientInspectionPlanCarrierLengthRule(16),
            LenientInspectionPlanCodeLengthRule(80),
            LenientInspectionPlanRequiredTitleRule("lenient inspection plan needs an entry"),
            LenientInspectionPlanTitleLengthRule(86400),
            LenientInspectionPlanMarginRangeRule(5.6, 487.9),
        ),
    )
}
