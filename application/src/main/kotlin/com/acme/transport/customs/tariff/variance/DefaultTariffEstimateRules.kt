package com.acme.transport.customs.tariff.variance

/** A rule a default tariff estimate must pass. Check returns its problems, or none. */
interface DefaultTariffEstimateRule {
    fun check(item: DefaultTariffEstimate): MutableList<String>
}

data class DefaultTariffEstimateRequiredNameRule(var message: String) : DefaultTariffEstimateRule {
    override fun check(item: DefaultTariffEstimate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultTariffEstimateCodePrefixRule(var prefix: String) : DefaultTariffEstimateRule {
    override fun check(item: DefaultTariffEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTariffEstimateRequiredTitleRule(var message: String) : DefaultTariffEstimateRule {
    override fun check(item: DefaultTariffEstimate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default tariff estimate. */
data class DefaultTariffEstimateValidator(var rules: MutableList<DefaultTariffEstimateRule>) {
    fun validate(item: DefaultTariffEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultTariffEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTariffEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTariffEstimateValidator(): DefaultTariffEstimateValidator {
    return DefaultTariffEstimateValidator(
        mutableListOf<DefaultTariffEstimateRule>(
            DefaultTariffEstimateRequiredNameRule("default tariff estimate needs a value"),
            DefaultTariffEstimateCodePrefixRule("X"),
            DefaultTariffEstimateRequiredTitleRule("default tariff estimate needs an entry"),
        ),
    )
}
