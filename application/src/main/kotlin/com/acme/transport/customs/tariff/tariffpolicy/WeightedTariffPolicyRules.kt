package com.acme.transport.customs.tariff.tariffpolicy

import com.acme.transport.kit.text.normalize

/** A rule a weighted tariff policy must pass. Check returns its problems, or none. */
interface WeightedTariffPolicyRule {
    fun check(item: WeightedTariffPolicy): MutableList<String>
}

data class WeightedTariffPolicyRequiredNameRule(var message: String) : WeightedTariffPolicyRule {
    override fun check(item: WeightedTariffPolicy): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedTariffPolicyCodeLengthRule(var maxLength: Int) : WeightedTariffPolicyRule {
    override fun check(item: WeightedTariffPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTariffPolicyAllowedCodeRule(var allowed: MutableList<String>) : WeightedTariffPolicyRule {
    override fun check(item: WeightedTariffPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTariffPolicyRequiredCodeRule(var message: String) : WeightedTariffPolicyRule {
    override fun check(item: WeightedTariffPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedTariffPolicyGraceDaysRangeRule(var min: Int, var max: Int) : WeightedTariffPolicyRule {
    override fun check(item: WeightedTariffPolicy): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a weighted tariff policy. */
data class WeightedTariffPolicyValidator(var rules: MutableList<WeightedTariffPolicyRule>) {
    fun validate(item: WeightedTariffPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedTariffPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedTariffPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedTariffPolicyValidator(): WeightedTariffPolicyValidator {
    return WeightedTariffPolicyValidator(
        mutableListOf<WeightedTariffPolicyRule>(
            WeightedTariffPolicyRequiredNameRule("weighted tariff policy needs this field"),
            WeightedTariffPolicyCodeLengthRule(120),
            WeightedTariffPolicyAllowedCodeRule(mutableListOf<String>()),
            WeightedTariffPolicyRequiredCodeRule("weighted tariff policy needs a value"),
            WeightedTariffPolicyGraceDaysRangeRule(120, 255),
        ),
    )
}
