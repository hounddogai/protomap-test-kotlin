package com.acme.transport.freightpay.ratecard.rollup

/** A rule a lenient rate card rule must pass. Check returns its problems, or none. */
interface LenientRateCardRuleRule {
    fun check(item: LenientRateCardRule): MutableList<String>
}

data class LenientRateCardRuleRequiredShiftCodeRule(var message: String) : LenientRateCardRuleRule {
    override fun check(item: LenientRateCardRule): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientRateCardRuleRequiredCodeRule(var message: String) : LenientRateCardRuleRule {
    override fun check(item: LenientRateCardRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientRateCardRuleShiftCodeLengthRule(var maxLength: Int) : LenientRateCardRuleRule {
    override fun check(item: LenientRateCardRule): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRateCardRuleEndsAtRangeRule(var min: Int, var max: Int) : LenientRateCardRuleRule {
    override fun check(item: LenientRateCardRule): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class LenientRateCardRuleMinimumRangeRule(var min: Double, var max: Double) : LenientRateCardRuleRule {
    override fun check(item: LenientRateCardRule): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class LenientRateCardRuleLabelLengthRule(var maxLength: Int) : LenientRateCardRuleRule {
    override fun check(item: LenientRateCardRule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient rate card rule. */
data class LenientRateCardRuleValidator(var rules: MutableList<LenientRateCardRuleRule>) {
    fun validate(item: LenientRateCardRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientRateCardRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRateCardRule>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRateCardRuleValidator(): LenientRateCardRuleValidator {
    return LenientRateCardRuleValidator(
        mutableListOf<LenientRateCardRuleRule>(
            LenientRateCardRuleRequiredShiftCodeRule("lenient rate card rule needs this field"),
            LenientRateCardRuleRequiredCodeRule("lenient rate card rule needs a value"),
            LenientRateCardRuleShiftCodeLengthRule(16),
            LenientRateCardRuleEndsAtRangeRule(64, 3600),
            LenientRateCardRuleMinimumRangeRule(7.3, 1510.0),
            LenientRateCardRuleLabelLengthRule(1000),
        ),
    )
}
