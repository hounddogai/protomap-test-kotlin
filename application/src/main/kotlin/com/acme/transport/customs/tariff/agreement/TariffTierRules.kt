package com.acme.transport.customs.tariff.agreement

/** A rule a tariff tier must pass. Check returns its problems, or none. */
interface TariffTierRule {
    fun check(item: TariffTier): MutableList<String>
}

data class TariffTierCodePrefixRule(var prefix: String) : TariffTierRule {
    override fun check(item: TariffTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TariffTierTitleLengthRule(var maxLength: Int) : TariffTierRule {
    override fun check(item: TariffTier): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffTierRequiredTitleRule(var message: String) : TariffTierRule {
    override fun check(item: TariffTier): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffTierEtaMinutesRangeRule(var min: Int, var max: Int) : TariffTierRule {
    override fun check(item: TariffTier): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class TariffTierDepotLengthRule(var maxLength: Int) : TariffTierRule {
    override fun check(item: TariffTier): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffTierBudgetRangeRule(var min: Double, var max: Double) : TariffTierRule {
    override fun check(item: TariffTier): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff tier. */
data class TariffTierValidator(var rules: MutableList<TariffTierRule>) {
    fun validate(item: TariffTier): MutableList<String> {
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

    fun requireValid(item: TariffTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffTier>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffTierValidator(): TariffTierValidator {
    return TariffTierValidator(
        mutableListOf<TariffTierRule>(
            TariffTierCodePrefixRule("OPS"),
            TariffTierTitleLengthRule(3600),
            TariffTierRequiredTitleRule("tariff tier needs this field"),
            TariffTierEtaMinutesRangeRule(120, 3600),
            TariffTierDepotLengthRule(32),
            TariffTierBudgetRangeRule(3.6, 2281.2),
        ),
    )
}
