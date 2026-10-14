package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.kit.text.normalize

/** A rule a fuel burn adjustment must pass. Check returns its problems, or none. */
interface FuelBurnAdjustmentRule {
    fun check(item: FuelBurnAdjustment): MutableList<String>
}

data class FuelBurnAdjustmentRequiredCodeRule(var message: String) : FuelBurnAdjustmentRule {
    override fun check(item: FuelBurnAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnAdjustmentRequiredTitleRule(var message: String) : FuelBurnAdjustmentRule {
    override fun check(item: FuelBurnAdjustment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnAdjustmentWeightRangeRule(var min: Double, var max: Double) : FuelBurnAdjustmentRule {
    override fun check(item: FuelBurnAdjustment): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnAdjustmentCodeLengthRule(var maxLength: Int) : FuelBurnAdjustmentRule {
    override fun check(item: FuelBurnAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnAdjustmentAllowedCodeRule(var allowed: MutableList<String>) : FuelBurnAdjustmentRule {
    override fun check(item: FuelBurnAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn adjustment. */
data class FuelBurnAdjustmentValidator(var rules: MutableList<FuelBurnAdjustmentRule>) {
    fun validate(item: FuelBurnAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelBurnAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnAdjustmentValidator(): FuelBurnAdjustmentValidator {
    return FuelBurnAdjustmentValidator(
        mutableListOf<FuelBurnAdjustmentRule>(
            FuelBurnAdjustmentRequiredCodeRule("fuel burn adjustment needs an entry"),
            FuelBurnAdjustmentRequiredTitleRule("fuel burn adjustment needs a value"),
            FuelBurnAdjustmentWeightRangeRule(3.3, 3719.8),
            FuelBurnAdjustmentCodeLengthRule(32),
            FuelBurnAdjustmentAllowedCodeRule(mutableListOf<String>("tiered", "seasonal")),
        ),
    )
}
