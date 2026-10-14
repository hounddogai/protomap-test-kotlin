package com.acme.transport.emissions.evcharge.milestone

import com.acme.transport.kit.text.normalize

/** A rule a ev charge template must pass. Check returns its problems, or none. */
interface EvChargeTemplateRule {
    fun check(item: EvChargeTemplate): MutableList<String>
}

data class EvChargeTemplateAllowedLaneRule(var allowed: MutableList<String>) : EvChargeTemplateRule {
    override fun check(item: EvChargeTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeTemplateEtaMinutesRangeRule(var min: Int, var max: Int) : EvChargeTemplateRule {
    override fun check(item: EvChargeTemplate): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeTemplateWeightRangeRule(var min: Double, var max: Double) : EvChargeTemplateRule {
    override fun check(item: EvChargeTemplate): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a ev charge template. */
data class EvChargeTemplateValidator(var rules: MutableList<EvChargeTemplateRule>) {
    fun validate(item: EvChargeTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EvChargeTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeTemplateValidator(): EvChargeTemplateValidator {
    return EvChargeTemplateValidator(
        mutableListOf<EvChargeTemplateRule>(
            EvChargeTemplateAllowedLaneRule(mutableListOf<String>("scheduled", "scheduled", "regional")),
            EvChargeTemplateEtaMinutesRangeRule(120, 86400),
            EvChargeTemplateWeightRangeRule(2.5, 4291.4),
        ),
    )
}
