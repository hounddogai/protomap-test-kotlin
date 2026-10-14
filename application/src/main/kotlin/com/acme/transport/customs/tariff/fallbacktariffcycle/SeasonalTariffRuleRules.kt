package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.kit.text.normalize

/** A rule a seasonal tariff rule must pass. Check returns its problems, or none. */
interface SeasonalTariffRuleRule {
    fun check(item: SeasonalTariffRule): MutableList<String>
}

data class SeasonalTariffRuleRequiredOwnerIdRule(var message: String) : SeasonalTariffRuleRule {
    override fun check(item: SeasonalTariffRule): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTariffRuleCodeLengthRule(var maxLength: Int) : SeasonalTariffRuleRule {
    override fun check(item: SeasonalTariffRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTariffRuleRequiredTitleRule(var message: String) : SeasonalTariffRuleRule {
    override fun check(item: SeasonalTariffRule): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTariffRuleAllowedOwnerIdRule(var allowed: MutableList<String>) : SeasonalTariffRuleRule {
    override fun check(item: SeasonalTariffRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTariffRuleCostRangeRule(var min: Double, var max: Double) : SeasonalTariffRuleRule {
    override fun check(item: SeasonalTariffRule): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a seasonal tariff rule. */
data class SeasonalTariffRuleValidator(var rules: MutableList<SeasonalTariffRuleRule>) {
    fun validate(item: SeasonalTariffRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalTariffRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTariffRule>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTariffRuleValidator(): SeasonalTariffRuleValidator {
    return SeasonalTariffRuleValidator(
        mutableListOf<SeasonalTariffRuleRule>(
            SeasonalTariffRuleRequiredOwnerIdRule("seasonal tariff rule needs an entry"),
            SeasonalTariffRuleCodeLengthRule(255),
            SeasonalTariffRuleRequiredTitleRule("seasonal tariff rule needs a value"),
            SeasonalTariffRuleAllowedOwnerIdRule(mutableListOf<String>("regional", "fallback", "default")),
            SeasonalTariffRuleCostRangeRule(9.0, 2396.6),
        ),
    )
}
