package com.acme.transport.customs.tariff.milestone

/** A rule a fallback tariff estimate must pass. Check returns its problems, or none. */
interface FallbackTariffEstimateRule {
    fun check(item: FallbackTariffEstimate): MutableList<String>
}

data class FallbackTariffEstimateCodePrefixRule(var prefix: String) : FallbackTariffEstimateRule {
    override fun check(item: FallbackTariffEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTariffEstimateRequiredOwnerIdRule(var message: String) : FallbackTariffEstimateRule {
    override fun check(item: FallbackTariffEstimate): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTariffEstimateVersionRangeRule(var min: Int, var max: Int) : FallbackTariffEstimateRule {
    override fun check(item: FallbackTariffEstimate): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class FallbackTariffEstimateRequiredCodeRule(var message: String) : FallbackTariffEstimateRule {
    override fun check(item: FallbackTariffEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback tariff estimate. */
data class FallbackTariffEstimateValidator(var rules: MutableList<FallbackTariffEstimateRule>) {
    fun validate(item: FallbackTariffEstimate): MutableList<String> {
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

    fun requireValid(item: FallbackTariffEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackTariffEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackTariffEstimateValidator(): FallbackTariffEstimateValidator {
    return FallbackTariffEstimateValidator(
        mutableListOf<FallbackTariffEstimateRule>(
            FallbackTariffEstimateCodePrefixRule("Q"),
            FallbackTariffEstimateRequiredOwnerIdRule("fallback tariff estimate needs this field"),
            FallbackTariffEstimateVersionRangeRule(120, 86400),
            FallbackTariffEstimateRequiredCodeRule("fallback tariff estimate needs this field"),
        ),
    )
}
