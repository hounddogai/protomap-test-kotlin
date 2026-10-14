package com.acme.transport.customs.tariff.plan

import com.acme.transport.kit.text.normalize

/** A rule a tariff plan must pass. Check returns its problems, or none. */
interface TariffPlanRule {
    fun check(item: TariffPlan): MutableList<String>
}

data class TariffPlanCodePrefixRule(var prefix: String) : TariffPlanRule {
    override fun check(item: TariffPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TariffPlanEndsAtRangeRule(var min: Int, var max: Int) : TariffPlanRule {
    override fun check(item: TariffPlan): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TariffPlanAllowedSourceRule(var allowed: MutableList<String>) : TariffPlanRule {
    override fun check(item: TariffPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tariff plan. */
data class TariffPlanValidator(var rules: MutableList<TariffPlanRule>) {
    fun validate(item: TariffPlan): MutableList<String> {
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

    fun requireValid(item: TariffPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TariffPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultTariffPlanValidator(): TariffPlanValidator {
    return TariffPlanValidator(
        mutableListOf<TariffPlanRule>(
            TariffPlanCodePrefixRule("X"),
            TariffPlanEndsAtRangeRule(120, 1000),
            TariffPlanAllowedSourceRule(mutableListOf<String>("batched")),
        ),
    )
}
