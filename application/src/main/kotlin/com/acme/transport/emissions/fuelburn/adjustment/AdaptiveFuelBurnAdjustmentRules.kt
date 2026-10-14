package com.acme.transport.emissions.fuelburn.adjustment

/** A rule a adaptive fuel burn adjustment must pass. Check returns its problems, or none. */
interface AdaptiveFuelBurnAdjustmentRule {
    fun check(item: AdaptiveFuelBurnAdjustment): MutableList<String>
}

data class AdaptiveFuelBurnAdjustmentRequiredDriverIdRule(
    var message: String,
) : AdaptiveFuelBurnAdjustmentRule {
    override fun check(item: AdaptiveFuelBurnAdjustment): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveFuelBurnAdjustmentDiscountRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveFuelBurnAdjustmentRule {
    override fun check(item: AdaptiveFuelBurnAdjustment): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveFuelBurnAdjustmentRequiredCodeRule(var message: String) : AdaptiveFuelBurnAdjustmentRule {
    override fun check(item: AdaptiveFuelBurnAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveFuelBurnAdjustmentCodePrefixRule(var prefix: String) : AdaptiveFuelBurnAdjustmentRule {
    override fun check(item: AdaptiveFuelBurnAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive fuel burn adjustment. */
data class AdaptiveFuelBurnAdjustmentValidator(var rules: MutableList<AdaptiveFuelBurnAdjustmentRule>) {
    fun validate(item: AdaptiveFuelBurnAdjustment): MutableList<String> {
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

    fun requireValid(item: AdaptiveFuelBurnAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveFuelBurnAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveFuelBurnAdjustmentValidator(): AdaptiveFuelBurnAdjustmentValidator {
    return AdaptiveFuelBurnAdjustmentValidator(
        mutableListOf<AdaptiveFuelBurnAdjustmentRule>(
            AdaptiveFuelBurnAdjustmentRequiredDriverIdRule("adaptive fuel burn adjustment needs this field"),
            AdaptiveFuelBurnAdjustmentDiscountRangeRule(8.8, 3211.6),
            AdaptiveFuelBurnAdjustmentRequiredCodeRule("adaptive fuel burn adjustment needs a value"),
            AdaptiveFuelBurnAdjustmentCodePrefixRule("ACM"),
        ),
    )
}
