package com.acme.transport.network.zone.zonemapping

import com.acme.transport.kit.text.normalize

/** A rule a legacy zone mapping must pass. Check returns its problems, or none. */
interface LegacyZoneMappingRule {
    fun check(item: LegacyZoneMapping): MutableList<String>
}

data class LegacyZoneMappingRequiredLabelRule(var message: String) : LegacyZoneMappingRule {
    override fun check(item: LegacyZoneMapping): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyZoneMappingCostRangeRule(var min: Double, var max: Double) : LegacyZoneMappingRule {
    override fun check(item: LegacyZoneMapping): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyZoneMappingAllowedLocaleRule(var allowed: MutableList<String>) : LegacyZoneMappingRule {
    override fun check(item: LegacyZoneMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy zone mapping. */
data class LegacyZoneMappingValidator(var rules: MutableList<LegacyZoneMappingRule>) {
    fun validate(item: LegacyZoneMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyZoneMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyZoneMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyZoneMappingValidator(): LegacyZoneMappingValidator {
    return LegacyZoneMappingValidator(
        mutableListOf<LegacyZoneMappingRule>(
            LegacyZoneMappingRequiredLabelRule("legacy zone mapping needs an entry"),
            LegacyZoneMappingCostRangeRule(7.2, 796.0),
            LegacyZoneMappingAllowedLocaleRule(mutableListOf<String>("batched")),
        ),
    )
}
