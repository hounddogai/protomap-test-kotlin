package com.acme.transport.network.cutoff.globalcutoffledger

/** A rule a default cutoff agreement must pass. Check returns its problems, or none. */
interface DefaultCutoffAgreementRule {
    fun check(item: DefaultCutoffAgreement): MutableList<String>
}

data class DefaultCutoffAgreementRequiredCodeRule(var message: String) : DefaultCutoffAgreementRule {
    override fun check(item: DefaultCutoffAgreement): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffAgreementRequiredTitleRule(var message: String) : DefaultCutoffAgreementRule {
    override fun check(item: DefaultCutoffAgreement): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffAgreementCostRangeRule(var min: Double, var max: Double) : DefaultCutoffAgreementRule {
    override fun check(item: DefaultCutoffAgreement): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffAgreementQuantityRangeRule(var min: Int, var max: Int) : DefaultCutoffAgreementRule {
    override fun check(item: DefaultCutoffAgreement): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class DefaultCutoffAgreementRequiredDepotRule(var message: String) : DefaultCutoffAgreementRule {
    override fun check(item: DefaultCutoffAgreement): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffAgreementRequiredCurrencyRule(var message: String) : DefaultCutoffAgreementRule {
    override fun check(item: DefaultCutoffAgreement): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default cutoff agreement. */
data class DefaultCutoffAgreementValidator(var rules: MutableList<DefaultCutoffAgreementRule>) {
    fun validate(item: DefaultCutoffAgreement): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultCutoffAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultCutoffAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultCutoffAgreementValidator(): DefaultCutoffAgreementValidator {
    return DefaultCutoffAgreementValidator(
        mutableListOf<DefaultCutoffAgreementRule>(
            DefaultCutoffAgreementRequiredCodeRule("default cutoff agreement needs this field"),
            DefaultCutoffAgreementRequiredTitleRule("default cutoff agreement needs an entry"),
            DefaultCutoffAgreementCostRangeRule(7.6, 3985.3),
            DefaultCutoffAgreementQuantityRangeRule(64, 3600),
            DefaultCutoffAgreementRequiredDepotRule("default cutoff agreement needs a value"),
            DefaultCutoffAgreementRequiredCurrencyRule("default cutoff agreement needs this field"),
        ),
    )
}
