package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.kit.text.normalize

/** A rule a clearance policy must pass. Check returns its problems, or none. */
interface ClearancePolicyRule {
    fun check(item: ClearancePolicy): MutableList<String>
}

data class ClearancePolicyDurationMinutesRangeRule(var min: Int, var max: Int) : ClearancePolicyRule {
    override fun check(item: ClearancePolicy): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class ClearancePolicyNameLengthRule(var maxLength: Int) : ClearancePolicyRule {
    override fun check(item: ClearancePolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearancePolicyAllowedCodeRule(var allowed: MutableList<String>) : ClearancePolicyRule {
    override fun check(item: ClearancePolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class ClearancePolicyRateRangeRule(var min: Double, var max: Double) : ClearancePolicyRule {
    override fun check(item: ClearancePolicy): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a clearance policy. */
data class ClearancePolicyValidator(var rules: MutableList<ClearancePolicyRule>) {
    fun validate(item: ClearancePolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ClearancePolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearancePolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultClearancePolicyValidator(): ClearancePolicyValidator {
    return ClearancePolicyValidator(
        mutableListOf<ClearancePolicyRule>(
            ClearancePolicyDurationMinutesRangeRule(120, 1000),
            ClearancePolicyNameLengthRule(3600),
            ClearancePolicyAllowedCodeRule(mutableListOf<String>("strict", "default", "fallback")),
            ClearancePolicyRateRangeRule(2.0, 4030.5),
        ),
    )
}
