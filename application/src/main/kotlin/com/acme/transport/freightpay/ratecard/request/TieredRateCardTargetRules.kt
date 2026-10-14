package com.acme.transport.freightpay.ratecard.request

/** A rule a tiered rate card target must pass. Check returns its problems, or none. */
interface TieredRateCardTargetRule {
    fun check(item: TieredRateCardTarget): MutableList<String>
}

data class TieredRateCardTargetMinimumRangeRule(var min: Double, var max: Double) : TieredRateCardTargetRule {
    override fun check(item: TieredRateCardTarget): MutableList<String> {
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

data class TieredRateCardTargetRequiredCodeRule(var message: String) : TieredRateCardTargetRule {
    override fun check(item: TieredRateCardTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredRateCardTargetCodePrefixRule(var prefix: String) : TieredRateCardTargetRule {
    override fun check(item: TieredRateCardTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredRateCardTargetRequiredNameRule(var message: String) : TieredRateCardTargetRule {
    override fun check(item: TieredRateCardTarget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered rate card target. */
data class TieredRateCardTargetValidator(var rules: MutableList<TieredRateCardTargetRule>) {
    fun validate(item: TieredRateCardTarget): MutableList<String> {
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

    fun requireValid(item: TieredRateCardTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredRateCardTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredRateCardTargetValidator(): TieredRateCardTargetValidator {
    return TieredRateCardTargetValidator(
        mutableListOf<TieredRateCardTargetRule>(
            TieredRateCardTargetMinimumRangeRule(9.6, 2889.2),
            TieredRateCardTargetRequiredCodeRule("tiered rate card target needs a value"),
            TieredRateCardTargetCodePrefixRule("REF"),
            TieredRateCardTargetRequiredNameRule("tiered rate card target needs this field"),
        ),
    )
}
