package com.acme.transport.customs.tariff.profile

/** A rule a tariff rule must pass. Check returns its problems, or none. */
interface TariffRuleRule {
    fun check(item: TariffRule): MutableList<String>
}

data class TariffRuleRequiredDriverIdRule(var message: String) : TariffRuleRule {
    override fun check(item: TariffRule): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TariffRuleCapacityRangeRule(var min: Int, var max: Int) : TariffRuleRule {
    override fun check(item: TariffRule): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TariffRuleTitleLengthRule(var maxLength: Int) : TariffRuleRule {
    override fun check(item: TariffRule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff rule. */
data class TariffRuleValidator(var rules: MutableList<TariffRuleRule>) {
    fun validate(item: TariffRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TariffRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffRule>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffRuleValidator(): TariffRuleValidator {
    return TariffRuleValidator(
        mutableListOf<TariffRuleRule>(
            TariffRuleRequiredDriverIdRule("tariff rule needs a value"),
            TariffRuleCapacityRangeRule(16, 64),
            TariffRuleTitleLengthRule(16),
        ),
    )
}
