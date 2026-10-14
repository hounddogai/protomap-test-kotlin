package com.acme.transport.freightpay.fuelsurcharge.proposal

/** A rule a tiered fuel surcharge target must pass. Check returns its problems, or none. */
interface TieredFuelSurchargeTargetRule {
    fun check(item: TieredFuelSurchargeTarget): MutableList<String>
}

data class TieredFuelSurchargeTargetRateRangeRule(
    var min: Double,
    var max: Double,
) : TieredFuelSurchargeTargetRule {
    override fun check(item: TieredFuelSurchargeTarget): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelSurchargeTargetRequiredLabelRule(var message: String) : TieredFuelSurchargeTargetRule {
    override fun check(item: TieredFuelSurchargeTarget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelSurchargeTargetLabelLengthRule(var maxLength: Int) : TieredFuelSurchargeTargetRule {
    override fun check(item: TieredFuelSurchargeTarget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelSurchargeTargetPalletsRangeRule(
    var min: Int,
    var max: Int,
) : TieredFuelSurchargeTargetRule {
    override fun check(item: TieredFuelSurchargeTarget): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class TieredFuelSurchargeTargetActiveGuardRule(var limit: Int) : TieredFuelSurchargeTargetRule {
    override fun check(item: TieredFuelSurchargeTarget): MutableList<String> {
        if (item.active && item.pallets > limit) {
            return mutableListOf<String>("pallets ${item.pallets} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered fuel surcharge target. */
data class TieredFuelSurchargeTargetValidator(var rules: MutableList<TieredFuelSurchargeTargetRule>) {
    fun validate(item: TieredFuelSurchargeTarget): MutableList<String> {
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

    fun requireValid(item: TieredFuelSurchargeTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredFuelSurchargeTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredFuelSurchargeTargetValidator(): TieredFuelSurchargeTargetValidator {
    return TieredFuelSurchargeTargetValidator(
        mutableListOf<TieredFuelSurchargeTargetRule>(
            TieredFuelSurchargeTargetRateRangeRule(2.4, 445.5),
            TieredFuelSurchargeTargetRequiredLabelRule("tiered fuel surcharge target needs this field"),
            TieredFuelSurchargeTargetLabelLengthRule(16),
            TieredFuelSurchargeTargetPalletsRangeRule(120, 3600),
            TieredFuelSurchargeTargetActiveGuardRule(64),
        ),
    )
}
