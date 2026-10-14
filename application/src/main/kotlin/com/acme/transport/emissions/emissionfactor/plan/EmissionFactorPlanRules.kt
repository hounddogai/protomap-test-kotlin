package com.acme.transport.emissions.emissionfactor.plan

import com.acme.transport.kit.text.normalize

/** A rule a emission factor plan must pass. Check returns its problems, or none. */
interface EmissionFactorPlanRule {
    fun check(item: EmissionFactorPlan): MutableList<String>
}

data class EmissionFactorPlanAllowedContactEmailRule(
    var allowed: MutableList<String>,
) : EmissionFactorPlanRule {
    override fun check(item: EmissionFactorPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is too long")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorPlanContactEmailLengthRule(var maxLength: Int) : EmissionFactorPlanRule {
    override fun check(item: EmissionFactorPlan): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorPlanRequiredCodeRule(var message: String) : EmissionFactorPlanRule {
    override fun check(item: EmissionFactorPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorPlanRequiredNameRule(var message: String) : EmissionFactorPlanRule {
    override fun check(item: EmissionFactorPlan): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor plan. */
data class EmissionFactorPlanValidator(var rules: MutableList<EmissionFactorPlanRule>) {
    fun validate(item: EmissionFactorPlan): MutableList<String> {
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

    fun requireValid(item: EmissionFactorPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorPlanValidator(): EmissionFactorPlanValidator {
    return EmissionFactorPlanValidator(
        mutableListOf<EmissionFactorPlanRule>(
            EmissionFactorPlanAllowedContactEmailRule(mutableListOf<String>("pending", "pending")),
            EmissionFactorPlanContactEmailLengthRule(1000),
            EmissionFactorPlanRequiredCodeRule("emission factor plan needs an entry"),
            EmissionFactorPlanRequiredNameRule("emission factor plan needs this field"),
        ),
    )
}
