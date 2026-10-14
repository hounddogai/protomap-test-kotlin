package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.kit.text.normalize

/** A rule a tiered inspection tier must pass. Check returns its problems, or none. */
interface TieredInspectionTierRule {
    fun check(item: TieredInspectionTier): MutableList<String>
}

data class TieredInspectionTierCategoryLengthRule(var maxLength: Int) : TieredInspectionTierRule {
    override fun check(item: TieredInspectionTier): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredInspectionTierDiscountRangeRule(var min: Double, var max: Double) : TieredInspectionTierRule {
    override fun check(item: TieredInspectionTier): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class TieredInspectionTierRequiredCountryRule(var message: String) : TieredInspectionTierRule {
    override fun check(item: TieredInspectionTier): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredInspectionTierAllowedNameRule(var allowed: MutableList<String>) : TieredInspectionTierRule {
    override fun check(item: TieredInspectionTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class TieredInspectionTierCodeLengthRule(var maxLength: Int) : TieredInspectionTierRule {
    override fun check(item: TieredInspectionTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered inspection tier. */
data class TieredInspectionTierValidator(var rules: MutableList<TieredInspectionTierRule>) {
    fun validate(item: TieredInspectionTier): MutableList<String> {
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

    fun requireValid(item: TieredInspectionTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredInspectionTier>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredInspectionTierValidator(): TieredInspectionTierValidator {
    return TieredInspectionTierValidator(
        mutableListOf<TieredInspectionTierRule>(
            TieredInspectionTierCategoryLengthRule(80),
            TieredInspectionTierDiscountRangeRule(1.7, 224.0),
            TieredInspectionTierRequiredCountryRule("tiered inspection tier needs a value"),
            TieredInspectionTierAllowedNameRule(mutableListOf<String>("strict")),
            TieredInspectionTierCodeLengthRule(32),
        ),
    )
}
