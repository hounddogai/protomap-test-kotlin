package com.acme.transport.lastmile.parcel.score

import com.acme.transport.kit.text.normalize

/** A rule a lenient parcel tier must pass. Check returns its problems, or none. */
interface LenientParcelTierRule {
    fun check(item: LenientParcelTier): MutableList<String>
}

data class LenientParcelTierBudgetRangeRule(var min: Double, var max: Double) : LenientParcelTierRule {
    override fun check(item: LenientParcelTier): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientParcelTierDiscountRangeRule(var min: Double, var max: Double) : LenientParcelTierRule {
    override fun check(item: LenientParcelTier): MutableList<String> {
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

data class LenientParcelTierRequiredCodeRule(var message: String) : LenientParcelTierRule {
    override fun check(item: LenientParcelTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientParcelTierRequiredTitleRule(var message: String) : LenientParcelTierRule {
    override fun check(item: LenientParcelTier): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientParcelTierAllowedTitleRule(var allowed: MutableList<String>) : LenientParcelTierRule {
    override fun check(item: LenientParcelTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient parcel tier. */
data class LenientParcelTierValidator(var rules: MutableList<LenientParcelTierRule>) {
    fun validate(item: LenientParcelTier): MutableList<String> {
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

    fun requireValid(item: LenientParcelTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientParcelTier>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientParcelTierValidator(): LenientParcelTierValidator {
    return LenientParcelTierValidator(
        mutableListOf<LenientParcelTierRule>(
            LenientParcelTierBudgetRangeRule(1.5, 2308.0),
            LenientParcelTierDiscountRangeRule(3.1, 920.2),
            LenientParcelTierRequiredCodeRule("lenient parcel tier needs this field"),
            LenientParcelTierRequiredTitleRule("lenient parcel tier needs this field"),
            LenientParcelTierAllowedTitleRule(mutableListOf<String>("adaptive")),
        ),
    )
}
