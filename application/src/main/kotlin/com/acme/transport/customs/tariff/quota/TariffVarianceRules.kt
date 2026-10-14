package com.acme.transport.customs.tariff.quota

/** A rule a tariff variance must pass. Check returns its problems, or none. */
interface TariffVarianceRule {
    fun check(item: TariffVariance): MutableList<String>
}

data class TariffVarianceCodePrefixRule(var prefix: String) : TariffVarianceRule {
    override fun check(item: TariffVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TariffVarianceDiscountRangeRule(var min: Double, var max: Double) : TariffVarianceRule {
    override fun check(item: TariffVariance): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TariffVarianceStartsAtRangeRule(var min: Int, var max: Int) : TariffVarianceRule {
    override fun check(item: TariffVariance): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff variance. */
data class TariffVarianceValidator(var rules: MutableList<TariffVarianceRule>) {
    fun validate(item: TariffVariance): MutableList<String> {
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

    fun requireValid(item: TariffVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffVarianceValidator(): TariffVarianceValidator {
    return TariffVarianceValidator(
        mutableListOf<TariffVarianceRule>(
            TariffVarianceCodePrefixRule("ACM"),
            TariffVarianceDiscountRangeRule(3.4, 2615.6),
            TariffVarianceStartsAtRangeRule(64, 86400),
        ),
    )
}
