package com.acme.transport.customs.tariff.quota

/** A rule a tariff estimate must pass. Check returns its problems, or none. */
interface TariffEstimateRule {
    fun check(item: TariffEstimate): MutableList<String>
}

data class TariffEstimateContactEmailLengthRule(var maxLength: Int) : TariffEstimateRule {
    override fun check(item: TariffEstimate): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TariffEstimateRequiredNameRule(var message: String) : TariffEstimateRule {
    override fun check(item: TariffEstimate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffEstimateCodeLengthRule(var maxLength: Int) : TariffEstimateRule {
    override fun check(item: TariffEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff estimate. */
data class TariffEstimateValidator(var rules: MutableList<TariffEstimateRule>) {
    fun validate(item: TariffEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TariffEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffEstimateValidator(): TariffEstimateValidator {
    return TariffEstimateValidator(
        mutableListOf<TariffEstimateRule>(
            TariffEstimateContactEmailLengthRule(32),
            TariffEstimateRequiredNameRule("tariff estimate needs this field"),
            TariffEstimateCodeLengthRule(86400),
        ),
    )
}
