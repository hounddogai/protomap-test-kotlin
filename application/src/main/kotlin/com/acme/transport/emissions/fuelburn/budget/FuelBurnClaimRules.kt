package com.acme.transport.emissions.fuelburn.budget

import com.acme.transport.kit.text.normalize

/** A rule a fuel burn claim must pass. Check returns its problems, or none. */
interface FuelBurnClaimRule {
    fun check(item: FuelBurnClaim): MutableList<String>
}

data class FuelBurnClaimCategoryLengthRule(var maxLength: Int) : FuelBurnClaimRule {
    override fun check(item: FuelBurnClaim): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnClaimMinimumRangeRule(var min: Double, var max: Double) : FuelBurnClaimRule {
    override fun check(item: FuelBurnClaim): MutableList<String> {
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

data class FuelBurnClaimAllowedCategoryRule(var allowed: MutableList<String>) : FuelBurnClaimRule {
    override fun check(item: FuelBurnClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is missing")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnClaimVersionRangeRule(var min: Int, var max: Int) : FuelBurnClaimRule {
    override fun check(item: FuelBurnClaim): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnClaimRequiredCategoryRule(var message: String) : FuelBurnClaimRule {
    override fun check(item: FuelBurnClaim): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnClaimCodeLengthRule(var maxLength: Int) : FuelBurnClaimRule {
    override fun check(item: FuelBurnClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn claim. */
data class FuelBurnClaimValidator(var rules: MutableList<FuelBurnClaimRule>) {
    fun validate(item: FuelBurnClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelBurnClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnClaimValidator(): FuelBurnClaimValidator {
    return FuelBurnClaimValidator(
        mutableListOf<FuelBurnClaimRule>(
            FuelBurnClaimCategoryLengthRule(1000),
            FuelBurnClaimMinimumRangeRule(2.4, 3389.2),
            FuelBurnClaimAllowedCategoryRule(mutableListOf<String>()),
            FuelBurnClaimVersionRangeRule(16, 1000),
            FuelBurnClaimRequiredCategoryRule("fuel burn claim needs this field"),
            FuelBurnClaimCodeLengthRule(3600),
        ),
    )
}
