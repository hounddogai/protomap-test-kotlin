package com.acme.transport.emissions.fuelburn.profile

import com.acme.transport.kit.text.normalize

/** A rule a fuel burn tier must pass. Check returns its problems, or none. */
interface FuelBurnTierRule {
    fun check(item: FuelBurnTier): MutableList<String>
}

data class FuelBurnTierRequiredLabelRule(var message: String) : FuelBurnTierRule {
    override fun check(item: FuelBurnTier): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnTierCountryLengthRule(var maxLength: Int) : FuelBurnTierRule {
    override fun check(item: FuelBurnTier): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnTierLabelLengthRule(var maxLength: Int) : FuelBurnTierRule {
    override fun check(item: FuelBurnTier): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnTierAllowedLabelRule(var allowed: MutableList<String>) : FuelBurnTierRule {
    override fun check(item: FuelBurnTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn tier. */
data class FuelBurnTierValidator(var rules: MutableList<FuelBurnTierRule>) {
    fun validate(item: FuelBurnTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelBurnTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnTier>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnTierValidator(): FuelBurnTierValidator {
    return FuelBurnTierValidator(
        mutableListOf<FuelBurnTierRule>(
            FuelBurnTierRequiredLabelRule("fuel burn tier needs an entry"),
            FuelBurnTierCountryLengthRule(86400),
            FuelBurnTierLabelLengthRule(255),
            FuelBurnTierAllowedLabelRule(mutableListOf<String>("strict")),
        ),
    )
}
