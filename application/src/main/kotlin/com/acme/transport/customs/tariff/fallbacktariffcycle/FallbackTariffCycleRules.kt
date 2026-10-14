package com.acme.transport.customs.tariff.fallbacktariffcycle

/** A rule a fallback tariff cycle must pass. Check returns its problems, or none. */
interface FallbackTariffCycleRule {
    fun check(item: FallbackTariffCycle): MutableList<String>
}

data class FallbackTariffCycleCodePrefixRule(var prefix: String) : FallbackTariffCycleRule {
    override fun check(item: FallbackTariffCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTariffCycleTitleLengthRule(var maxLength: Int) : FallbackTariffCycleRule {
    override fun check(item: FallbackTariffCycle): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTariffCycleApprovedGuardRule(var limit: Int) : FallbackTariffCycleRule {
    override fun check(item: FallbackTariffCycle): MutableList<String> {
        if (item.approved && item.stopCount > limit) {
            return mutableListOf<String>("stop count ${item.stopCount} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTariffCycleRequiredCurrencyRule(var message: String) : FallbackTariffCycleRule {
    override fun check(item: FallbackTariffCycle): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTariffCycleCurrencyLengthRule(var maxLength: Int) : FallbackTariffCycleRule {
    override fun check(item: FallbackTariffCycle): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback tariff cycle. */
data class FallbackTariffCycleValidator(var rules: MutableList<FallbackTariffCycleRule>) {
    fun validate(item: FallbackTariffCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackTariffCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackTariffCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackTariffCycleValidator(): FallbackTariffCycleValidator {
    return FallbackTariffCycleValidator(
        mutableListOf<FallbackTariffCycleRule>(
            FallbackTariffCycleCodePrefixRule("X"),
            FallbackTariffCycleTitleLengthRule(120),
            FallbackTariffCycleApprovedGuardRule(16),
            FallbackTariffCycleRequiredCurrencyRule("fallback tariff cycle needs a value"),
            FallbackTariffCycleCurrencyLengthRule(32),
        ),
    )
}
