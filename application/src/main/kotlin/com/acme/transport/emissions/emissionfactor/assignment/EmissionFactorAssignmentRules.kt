package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.kit.text.normalize

/** A rule a emission factor assignment must pass. Check returns its problems, or none. */
interface EmissionFactorAssignmentRule {
    fun check(item: EmissionFactorAssignment): MutableList<String>
}

data class EmissionFactorAssignmentAllowedNameRule(
    var allowed: MutableList<String>,
) : EmissionFactorAssignmentRule {
    override fun check(item: EmissionFactorAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorAssignmentExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : EmissionFactorAssignmentRule {
    override fun check(item: EmissionFactorAssignment): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorAssignmentRequiredCodeRule(var message: String) : EmissionFactorAssignmentRule {
    override fun check(item: EmissionFactorAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor assignment. */
data class EmissionFactorAssignmentValidator(var rules: MutableList<EmissionFactorAssignmentRule>) {
    fun validate(item: EmissionFactorAssignment): MutableList<String> {
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

    fun requireValid(item: EmissionFactorAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorAssignmentValidator(): EmissionFactorAssignmentValidator {
    return EmissionFactorAssignmentValidator(
        mutableListOf<EmissionFactorAssignmentRule>(
            EmissionFactorAssignmentAllowedNameRule(mutableListOf<String>("adaptive", "fallback", "tiered")),
            EmissionFactorAssignmentExpiresAtRangeRule(64, 3600),
            EmissionFactorAssignmentRequiredCodeRule("emission factor assignment needs an entry"),
        ),
    )
}
