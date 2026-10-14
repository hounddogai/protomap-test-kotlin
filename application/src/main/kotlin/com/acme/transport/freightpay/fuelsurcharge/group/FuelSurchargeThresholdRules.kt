package com.acme.transport.freightpay.fuelsurcharge.group

/** A rule a fuel surcharge threshold must pass. Check returns its problems, or none. */
interface FuelSurchargeThresholdRule {
    fun check(item: FuelSurchargeThreshold): MutableList<String>
}

data class FuelSurchargeThresholdCodePrefixRule(var prefix: String) : FuelSurchargeThresholdRule {
    override fun check(item: FuelSurchargeThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeThresholdPriorityRangeRule(var min: Int, var max: Int) : FuelSurchargeThresholdRule {
    override fun check(item: FuelSurchargeThreshold): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class FuelSurchargeThresholdCodeLengthRule(var maxLength: Int) : FuelSurchargeThresholdRule {
    override fun check(item: FuelSurchargeThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeThresholdRequiredContactEmailRule(var message: String) : FuelSurchargeThresholdRule {
    override fun check(item: FuelSurchargeThreshold): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeThresholdRequiredCurrencyRule(var message: String) : FuelSurchargeThresholdRule {
    override fun check(item: FuelSurchargeThreshold): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeThresholdRequiredCodeRule(var message: String) : FuelSurchargeThresholdRule {
    override fun check(item: FuelSurchargeThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge threshold. */
data class FuelSurchargeThresholdValidator(var rules: MutableList<FuelSurchargeThresholdRule>) {
    fun validate(item: FuelSurchargeThreshold): MutableList<String> {
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

    fun requireValid(item: FuelSurchargeThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeThresholdValidator(): FuelSurchargeThresholdValidator {
    return FuelSurchargeThresholdValidator(
        mutableListOf<FuelSurchargeThresholdRule>(
            FuelSurchargeThresholdCodePrefixRule("ACM"),
            FuelSurchargeThresholdPriorityRangeRule(32, 64),
            FuelSurchargeThresholdCodeLengthRule(64),
            FuelSurchargeThresholdRequiredContactEmailRule("fuel surcharge threshold needs an entry"),
            FuelSurchargeThresholdRequiredCurrencyRule("fuel surcharge threshold needs a value"),
            FuelSurchargeThresholdRequiredCodeRule("fuel surcharge threshold needs a value"),
        ),
    )
}
