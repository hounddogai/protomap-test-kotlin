package com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment

/** A rule a fuel surcharge rule must pass. Check returns its problems, or none. */
interface FuelSurchargeRuleRule {
    fun check(item: FuelSurchargeRule): MutableList<String>
}

data class FuelSurchargeRuleRequiredLabelRule(var message: String) : FuelSurchargeRuleRule {
    override fun check(item: FuelSurchargeRule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeRuleLabelLengthRule(var maxLength: Int) : FuelSurchargeRuleRule {
    override fun check(item: FuelSurchargeRule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeRuleVehicleIdLengthRule(var maxLength: Int) : FuelSurchargeRuleRule {
    override fun check(item: FuelSurchargeRule): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeRuleMaximumRangeRule(var min: Double, var max: Double) : FuelSurchargeRuleRule {
    override fun check(item: FuelSurchargeRule): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeRuleRequiredCodeRule(var message: String) : FuelSurchargeRuleRule {
    override fun check(item: FuelSurchargeRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeRuleStopCountRangeRule(var min: Int, var max: Int) : FuelSurchargeRuleRule {
    override fun check(item: FuelSurchargeRule): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge rule. */
data class FuelSurchargeRuleValidator(var rules: MutableList<FuelSurchargeRuleRule>) {
    fun validate(item: FuelSurchargeRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelSurchargeRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeRule>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeRuleValidator(): FuelSurchargeRuleValidator {
    return FuelSurchargeRuleValidator(
        mutableListOf<FuelSurchargeRuleRule>(
            FuelSurchargeRuleRequiredLabelRule("fuel surcharge rule needs this field"),
            FuelSurchargeRuleLabelLengthRule(1000),
            FuelSurchargeRuleVehicleIdLengthRule(16),
            FuelSurchargeRuleMaximumRangeRule(3.5, 4576.9),
            FuelSurchargeRuleRequiredCodeRule("fuel surcharge rule needs a value"),
            FuelSurchargeRuleStopCountRangeRule(80, 120),
        ),
    )
}
