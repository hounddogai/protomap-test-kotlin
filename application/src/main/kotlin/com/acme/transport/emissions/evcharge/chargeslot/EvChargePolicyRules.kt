package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.kit.text.normalize

/** A rule a ev charge policy must pass. Check returns its problems, or none. */
interface EvChargePolicyRule {
    fun check(item: EvChargePolicy): MutableList<String>
}

data class EvChargePolicyAmountRangeRule(var min: Double, var max: Double) : EvChargePolicyRule {
    override fun check(item: EvChargePolicy): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class EvChargePolicyCodeLengthRule(var maxLength: Int) : EvChargePolicyRule {
    override fun check(item: EvChargePolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargePolicyRequiredTrailerIdRule(var message: String) : EvChargePolicyRule {
    override fun check(item: EvChargePolicy): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargePolicyAllowedCodeRule(var allowed: MutableList<String>) : EvChargePolicyRule {
    override fun check(item: EvChargePolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge policy. */
data class EvChargePolicyValidator(var rules: MutableList<EvChargePolicyRule>) {
    fun validate(item: EvChargePolicy): MutableList<String> {
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

    fun requireValid(item: EvChargePolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargePolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargePolicyValidator(): EvChargePolicyValidator {
    return EvChargePolicyValidator(
        mutableListOf<EvChargePolicyRule>(
            EvChargePolicyAmountRangeRule(8.8, 4872.6),
            EvChargePolicyCodeLengthRule(64),
            EvChargePolicyRequiredTrailerIdRule("ev charge policy needs a value"),
            EvChargePolicyAllowedCodeRule(mutableListOf<String>("regional", "legacy")),
        ),
    )
}
