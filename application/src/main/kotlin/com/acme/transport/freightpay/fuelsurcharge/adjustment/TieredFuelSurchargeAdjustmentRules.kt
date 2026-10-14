package com.acme.transport.freightpay.fuelsurcharge.adjustment

/** A rule a tiered fuel surcharge adjustment must pass. Check returns its problems, or none. */
interface TieredFuelSurchargeAdjustmentRule {
    fun check(item: TieredFuelSurchargeAdjustment): MutableList<String>
}

data class TieredFuelSurchargeAdjustmentCodePrefixRule(var prefix: String) : TieredFuelSurchargeAdjustmentRule {
    override fun check(item: TieredFuelSurchargeAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelSurchargeAdjustmentRequiredPhoneRule(
    var message: String,
) : TieredFuelSurchargeAdjustmentRule {
    override fun check(item: TieredFuelSurchargeAdjustment): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelSurchargeAdjustmentPhoneLengthRule(
    var maxLength: Int,
) : TieredFuelSurchargeAdjustmentRule {
    override fun check(item: TieredFuelSurchargeAdjustment): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelSurchargeAdjustmentStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : TieredFuelSurchargeAdjustmentRule {
    override fun check(item: TieredFuelSurchargeAdjustment): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelSurchargeAdjustmentRequiredCodeRule(
    var message: String,
) : TieredFuelSurchargeAdjustmentRule {
    override fun check(item: TieredFuelSurchargeAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered fuel surcharge adjustment. */
data class TieredFuelSurchargeAdjustmentValidator(var rules: MutableList<TieredFuelSurchargeAdjustmentRule>) {
    fun validate(item: TieredFuelSurchargeAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredFuelSurchargeAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredFuelSurchargeAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredFuelSurchargeAdjustmentValidator(): TieredFuelSurchargeAdjustmentValidator {
    return TieredFuelSurchargeAdjustmentValidator(
        mutableListOf<TieredFuelSurchargeAdjustmentRule>(
            TieredFuelSurchargeAdjustmentCodePrefixRule("REF"),
            TieredFuelSurchargeAdjustmentRequiredPhoneRule("tiered fuel surcharge adjustment needs a value"),
            TieredFuelSurchargeAdjustmentPhoneLengthRule(64),
            TieredFuelSurchargeAdjustmentStartsAtRangeRule(32, 120),
            TieredFuelSurchargeAdjustmentRequiredCodeRule("tiered fuel surcharge adjustment needs an entry"),
        ),
    )
}
