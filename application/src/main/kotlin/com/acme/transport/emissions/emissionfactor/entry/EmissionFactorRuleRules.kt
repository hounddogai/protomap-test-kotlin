package com.acme.transport.emissions.emissionfactor.entry

import com.acme.transport.kit.text.normalize

/** A rule a emission factor rule must pass. Check returns its problems, or none. */
interface EmissionFactorRuleRule {
    fun check(item: EmissionFactorRule): MutableList<String>
}

data class EmissionFactorRuleCodePrefixRule(var prefix: String) : EmissionFactorRuleRule {
    override fun check(item: EmissionFactorRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorRuleAllowedCodeRule(var allowed: MutableList<String>) : EmissionFactorRuleRule {
    override fun check(item: EmissionFactorRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorRuleFuelLitersRangeRule(var min: Double, var max: Double) : EmissionFactorRuleRule {
    override fun check(item: EmissionFactorRule): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class EmissionFactorRuleRequiredNameRule(var message: String) : EmissionFactorRuleRule {
    override fun check(item: EmissionFactorRule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorRuleChannelLengthRule(var maxLength: Int) : EmissionFactorRuleRule {
    override fun check(item: EmissionFactorRule): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor rule. */
data class EmissionFactorRuleValidator(var rules: MutableList<EmissionFactorRuleRule>) {
    fun validate(item: EmissionFactorRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EmissionFactorRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorRule>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorRuleValidator(): EmissionFactorRuleValidator {
    return EmissionFactorRuleValidator(
        mutableListOf<EmissionFactorRuleRule>(
            EmissionFactorRuleCodePrefixRule("X"),
            EmissionFactorRuleAllowedCodeRule(mutableListOf<String>("fallback", "weighted", "global")),
            EmissionFactorRuleFuelLitersRangeRule(3.7, 2456.5),
            EmissionFactorRuleRequiredNameRule("emission factor rule needs an entry"),
            EmissionFactorRuleChannelLengthRule(64),
        ),
    )
}
