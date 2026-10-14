package com.acme.transport.customs.inspection.pendinginspectionrule

/** A rule a adaptive inspection rule must pass. Check returns its problems, or none. */
interface AdaptiveInspectionRuleRule {
    fun check(item: AdaptiveInspectionRule): MutableList<String>
}

data class AdaptiveInspectionRuleRequiredCodeRule(var message: String) : AdaptiveInspectionRuleRule {
    override fun check(item: AdaptiveInspectionRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionRuleScoreRangeRule(var min: Double, var max: Double) : AdaptiveInspectionRuleRule {
    override fun check(item: AdaptiveInspectionRule): MutableList<String> {
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

data class AdaptiveInspectionRuleCodeLengthRule(var maxLength: Int) : AdaptiveInspectionRuleRule {
    override fun check(item: AdaptiveInspectionRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionRuleOdometerRangeRule(var min: Int, var max: Int) : AdaptiveInspectionRuleRule {
    override fun check(item: AdaptiveInspectionRule): MutableList<String> {
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

data class AdaptiveInspectionRulePinnedGuardRule(var limit: Int) : AdaptiveInspectionRuleRule {
    override fun check(item: AdaptiveInspectionRule): MutableList<String> {
        if (item.pinned && item.windowDays > limit) {
            return mutableListOf<String>("window days ${item.windowDays} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionRuleCarrierLengthRule(var maxLength: Int) : AdaptiveInspectionRuleRule {
    override fun check(item: AdaptiveInspectionRule): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive inspection rule. */
data class AdaptiveInspectionRuleValidator(var rules: MutableList<AdaptiveInspectionRuleRule>) {
    fun validate(item: AdaptiveInspectionRule): MutableList<String> {
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

    fun requireValid(item: AdaptiveInspectionRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveInspectionRule>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveInspectionRuleValidator(): AdaptiveInspectionRuleValidator {
    return AdaptiveInspectionRuleValidator(
        mutableListOf<AdaptiveInspectionRuleRule>(
            AdaptiveInspectionRuleRequiredCodeRule("adaptive inspection rule needs a value"),
            AdaptiveInspectionRuleScoreRangeRule(9.6, 1550.9),
            AdaptiveInspectionRuleCodeLengthRule(16),
            AdaptiveInspectionRuleOdometerRangeRule(64, 86400),
            AdaptiveInspectionRulePinnedGuardRule(16),
            AdaptiveInspectionRuleCarrierLengthRule(1000),
        ),
    )
}
