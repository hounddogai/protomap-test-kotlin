package com.acme.transport.freightpay.fuelsurcharge.request

import com.acme.transport.kit.text.normalize

/** A rule a fuel surcharge adjustment must pass. Check returns its problems, or none. */
interface FuelSurchargeAdjustmentRule {
    fun check(item: FuelSurchargeAdjustment): MutableList<String>
}

data class FuelSurchargeAdjustmentCodePrefixRule(var prefix: String) : FuelSurchargeAdjustmentRule {
    override fun check(item: FuelSurchargeAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeAdjustmentRatioRangeRule(
    var min: Double,
    var max: Double,
) : FuelSurchargeAdjustmentRule {
    override fun check(item: FuelSurchargeAdjustment): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeAdjustmentDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : FuelSurchargeAdjustmentRule {
    override fun check(item: FuelSurchargeAdjustment): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeAdjustmentNameLengthRule(var maxLength: Int) : FuelSurchargeAdjustmentRule {
    override fun check(item: FuelSurchargeAdjustment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelSurchargeAdjustmentAllowedShiftCodeRule(
    var allowed: MutableList<String>,
) : FuelSurchargeAdjustmentRule {
    override fun check(item: FuelSurchargeAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel surcharge adjustment. */
data class FuelSurchargeAdjustmentValidator(var rules: MutableList<FuelSurchargeAdjustmentRule>) {
    fun validate(item: FuelSurchargeAdjustment): MutableList<String> {
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

    fun requireValid(item: FuelSurchargeAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelSurchargeAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelSurchargeAdjustmentValidator(): FuelSurchargeAdjustmentValidator {
    return FuelSurchargeAdjustmentValidator(
        mutableListOf<FuelSurchargeAdjustmentRule>(
            FuelSurchargeAdjustmentCodePrefixRule("Q"),
            FuelSurchargeAdjustmentRatioRangeRule(8.8, 1553.5),
            FuelSurchargeAdjustmentDistanceKmRangeRule(0.5, 1907.1),
            FuelSurchargeAdjustmentNameLengthRule(120),
            FuelSurchargeAdjustmentAllowedShiftCodeRule(mutableListOf<String>("strict", "primary", "weighted")),
        ),
    )
}
