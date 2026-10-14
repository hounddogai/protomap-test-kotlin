package com.acme.transport.freightpay.fuelsurcharge.rule

/** A rule a regional fuel surcharge rule must pass. Check returns its problems, or none. */
interface RegionalFuelSurchargeRuleRule {
    fun check(item: RegionalFuelSurchargeRule): MutableList<String>
}

data class RegionalFuelSurchargeRuleCodeLengthRule(var maxLength: Int) : RegionalFuelSurchargeRuleRule {
    override fun check(item: RegionalFuelSurchargeRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelSurchargeRuleMinimumRangeRule(
    var min: Double,
    var max: Double,
) : RegionalFuelSurchargeRuleRule {
    override fun check(item: RegionalFuelSurchargeRule): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelSurchargeRuleCountryLengthRule(var maxLength: Int) : RegionalFuelSurchargeRuleRule {
    override fun check(item: RegionalFuelSurchargeRule): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional fuel surcharge rule. */
data class RegionalFuelSurchargeRuleValidator(var rules: MutableList<RegionalFuelSurchargeRuleRule>) {
    fun validate(item: RegionalFuelSurchargeRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalFuelSurchargeRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalFuelSurchargeRule>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalFuelSurchargeRuleValidator(): RegionalFuelSurchargeRuleValidator {
    return RegionalFuelSurchargeRuleValidator(
        mutableListOf<RegionalFuelSurchargeRuleRule>(
            RegionalFuelSurchargeRuleCodeLengthRule(120),
            RegionalFuelSurchargeRuleMinimumRangeRule(3.6, 1528.3),
            RegionalFuelSurchargeRuleCountryLengthRule(86400),
        ),
    )
}
