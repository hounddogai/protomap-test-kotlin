package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

/** A rule a tiered fuel surcharge baseline must pass. Check returns its problems, or none. */
interface TieredFuelSurchargeBaselineRule {
    fun check(item: TieredFuelSurchargeBaseline): MutableList<String>
}

data class TieredFuelSurchargeBaselineDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : TieredFuelSurchargeBaselineRule {
    override fun check(item: TieredFuelSurchargeBaseline): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class TieredFuelSurchargeBaselineCurrencyLengthRule(var maxLength: Int) : TieredFuelSurchargeBaselineRule {
    override fun check(item: TieredFuelSurchargeBaseline): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelSurchargeBaselineRequiredCodeRule(var message: String) : TieredFuelSurchargeBaselineRule {
    override fun check(item: TieredFuelSurchargeBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelSurchargeBaselineRequiredOwnerIdRule(
    var message: String,
) : TieredFuelSurchargeBaselineRule {
    override fun check(item: TieredFuelSurchargeBaseline): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered fuel surcharge baseline. */
data class TieredFuelSurchargeBaselineValidator(var rules: MutableList<TieredFuelSurchargeBaselineRule>) {
    fun validate(item: TieredFuelSurchargeBaseline): MutableList<String> {
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

    fun requireValid(item: TieredFuelSurchargeBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredFuelSurchargeBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredFuelSurchargeBaselineValidator(): TieredFuelSurchargeBaselineValidator {
    return TieredFuelSurchargeBaselineValidator(
        mutableListOf<TieredFuelSurchargeBaselineRule>(
            TieredFuelSurchargeBaselineDistanceKmRangeRule(9.8, 1616.2),
            TieredFuelSurchargeBaselineCurrencyLengthRule(32),
            TieredFuelSurchargeBaselineRequiredCodeRule("tiered fuel surcharge baseline needs a value"),
            TieredFuelSurchargeBaselineRequiredOwnerIdRule("tiered fuel surcharge baseline needs an entry"),
        ),
    )
}
