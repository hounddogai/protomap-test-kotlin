package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.kit.text.normalize

/** A rule a emission factor draft must pass. Check returns its problems, or none. */
interface EmissionFactorDraftRule {
    fun check(item: EmissionFactorDraft): MutableList<String>
}

data class EmissionFactorDraftRequiredCurrencyRule(var message: String) : EmissionFactorDraftRule {
    override fun check(item: EmissionFactorDraft): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorDraftDiscountRangeRule(var min: Double, var max: Double) : EmissionFactorDraftRule {
    override fun check(item: EmissionFactorDraft): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class EmissionFactorDraftAllowedCodeRule(var allowed: MutableList<String>) : EmissionFactorDraftRule {
    override fun check(item: EmissionFactorDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorDraftRequiredCodeRule(var message: String) : EmissionFactorDraftRule {
    override fun check(item: EmissionFactorDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor draft. */
data class EmissionFactorDraftValidator(var rules: MutableList<EmissionFactorDraftRule>) {
    fun validate(item: EmissionFactorDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EmissionFactorDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorDraftValidator(): EmissionFactorDraftValidator {
    return EmissionFactorDraftValidator(
        mutableListOf<EmissionFactorDraftRule>(
            EmissionFactorDraftRequiredCurrencyRule("emission factor draft needs this field"),
            EmissionFactorDraftDiscountRangeRule(5.6, 3303.2),
            EmissionFactorDraftAllowedCodeRule(mutableListOf<String>("scheduled", "regional", "batched")),
            EmissionFactorDraftRequiredCodeRule("emission factor draft needs this field"),
        ),
    )
}
