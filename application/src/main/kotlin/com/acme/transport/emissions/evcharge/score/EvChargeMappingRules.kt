package com.acme.transport.emissions.evcharge.score

import com.acme.transport.kit.text.normalize

/** A rule a ev charge mapping must pass. Check returns its problems, or none. */
interface EvChargeMappingRule {
    fun check(item: EvChargeMapping): MutableList<String>
}

data class EvChargeMappingAllowedCodeRule(var allowed: MutableList<String>) : EvChargeMappingRule {
    override fun check(item: EvChargeMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeMappingMinimumRangeRule(var min: Double, var max: Double) : EvChargeMappingRule {
    override fun check(item: EvChargeMapping): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class EvChargeMappingCodeLengthRule(var maxLength: Int) : EvChargeMappingRule {
    override fun check(item: EvChargeMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeMappingRequiredNameRule(var message: String) : EvChargeMappingRule {
    override fun check(item: EvChargeMapping): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge mapping. */
data class EvChargeMappingValidator(var rules: MutableList<EvChargeMappingRule>) {
    fun validate(item: EvChargeMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EvChargeMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeMappingValidator(): EvChargeMappingValidator {
    return EvChargeMappingValidator(
        mutableListOf<EvChargeMappingRule>(
            EvChargeMappingAllowedCodeRule(mutableListOf<String>("seasonal", "regional")),
            EvChargeMappingMinimumRangeRule(9.6, 3551.1),
            EvChargeMappingCodeLengthRule(80),
            EvChargeMappingRequiredNameRule("ev charge mapping needs this field"),
        ),
    )
}
