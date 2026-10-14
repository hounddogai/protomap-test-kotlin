package com.acme.transport.customs.inspection.rule

/** A rule a inspection rule must pass. Check returns its problems, or none. */
interface InspectionRuleRule {
    fun check(item: InspectionRule): MutableList<String>
}

data class InspectionRuleBudgetRangeRule(var min: Double, var max: Double) : InspectionRuleRule {
    override fun check(item: InspectionRule): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionRuleRequiredContactEmailRule(var message: String) : InspectionRuleRule {
    override fun check(item: InspectionRule): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionRuleRequiredShiftCodeRule(var message: String) : InspectionRuleRule {
    override fun check(item: InspectionRule): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionRuleRequiredCodeRule(var message: String) : InspectionRuleRule {
    override fun check(item: InspectionRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionRuleLabelLengthRule(var maxLength: Int) : InspectionRuleRule {
    override fun check(item: InspectionRule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionRuleCodePrefixRule(var prefix: String) : InspectionRuleRule {
    override fun check(item: InspectionRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection rule. */
data class InspectionRuleValidator(var rules: MutableList<InspectionRuleRule>) {
    fun validate(item: InspectionRule): MutableList<String> {
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

    fun requireValid(item: InspectionRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionRule>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionRuleValidator(): InspectionRuleValidator {
    return InspectionRuleValidator(
        mutableListOf<InspectionRuleRule>(
            InspectionRuleBudgetRangeRule(1.4, 2907.0),
            InspectionRuleRequiredContactEmailRule("inspection rule needs this field"),
            InspectionRuleRequiredShiftCodeRule("inspection rule needs an entry"),
            InspectionRuleRequiredCodeRule("inspection rule needs this field"),
            InspectionRuleLabelLengthRule(3600),
            InspectionRuleCodePrefixRule("REF"),
        ),
    )
}
