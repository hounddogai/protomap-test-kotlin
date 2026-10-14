package com.acme.transport.freightpay.fuelsurcharge.target

/** A rule a fuel surcharge mapping must pass. Check returns its problems, or none. */
interface FuelSurchargeMappingRule {
    fun check(item: FuelSurchargeMapping): MutableList<String>
}

data class FuelSurchargeMappingCodeLengthRule(var maxLength: Int) : FuelSurchargeMappingRule {
    override fun check(item: FuelSurchargeMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeMappingRequiredExternalRefRule(var message: String) : FuelSurchargeMappingRule {
    override fun check(item: FuelSurchargeMapping): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeMappingRateRangeRule(var min: Double, var max: Double) : FuelSurchargeMappingRule {
    override fun check(item: FuelSurchargeMapping): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeMappingRequiredLabelRule(var message: String) : FuelSurchargeMappingRule {
    override fun check(item: FuelSurchargeMapping): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeMappingExternalRefLengthRule(var maxLength: Int) : FuelSurchargeMappingRule {
    override fun check(item: FuelSurchargeMapping): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeMappingCodePrefixRule(var prefix: String) : FuelSurchargeMappingRule {
    override fun check(item: FuelSurchargeMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge mapping. */
data class FuelSurchargeMappingValidator(var rules: MutableList<FuelSurchargeMappingRule>) {
    fun validate(item: FuelSurchargeMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelSurchargeMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeMappingValidator(): FuelSurchargeMappingValidator {
    return FuelSurchargeMappingValidator(
        mutableListOf<FuelSurchargeMappingRule>(
            FuelSurchargeMappingCodeLengthRule(32),
            FuelSurchargeMappingRequiredExternalRefRule("fuel surcharge mapping needs an entry"),
            FuelSurchargeMappingRateRangeRule(7.9, 1824.1),
            FuelSurchargeMappingRequiredLabelRule("fuel surcharge mapping needs an entry"),
            FuelSurchargeMappingExternalRefLengthRule(16),
            FuelSurchargeMappingCodePrefixRule("OPS"),
        ),
    )
}
