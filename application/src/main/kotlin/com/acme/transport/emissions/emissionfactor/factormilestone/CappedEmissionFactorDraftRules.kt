package com.acme.transport.emissions.emissionfactor.factormilestone

import com.acme.transport.kit.text.normalize

/** A rule a capped emission factor draft must pass. Check returns its problems, or none. */
interface CappedEmissionFactorDraftRule {
    fun check(item: CappedEmissionFactorDraft): MutableList<String>
}

data class CappedEmissionFactorDraftCodePrefixRule(var prefix: String) : CappedEmissionFactorDraftRule {
    override fun check(item: CappedEmissionFactorDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedEmissionFactorDraftAllowedLabelRule(
    var allowed: MutableList<String>,
) : CappedEmissionFactorDraftRule {
    override fun check(item: CappedEmissionFactorDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class CappedEmissionFactorDraftRequiredLabelRule(var message: String) : CappedEmissionFactorDraftRule {
    override fun check(item: CappedEmissionFactorDraft): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedEmissionFactorDraftRequiredDriverIdRule(var message: String) : CappedEmissionFactorDraftRule {
    override fun check(item: CappedEmissionFactorDraft): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped emission factor draft. */
data class CappedEmissionFactorDraftValidator(var rules: MutableList<CappedEmissionFactorDraftRule>) {
    fun validate(item: CappedEmissionFactorDraft): MutableList<String> {
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

    fun requireValid(item: CappedEmissionFactorDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedEmissionFactorDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedEmissionFactorDraftValidator(): CappedEmissionFactorDraftValidator {
    return CappedEmissionFactorDraftValidator(
        mutableListOf<CappedEmissionFactorDraftRule>(
            CappedEmissionFactorDraftCodePrefixRule("OPS"),
            CappedEmissionFactorDraftAllowedLabelRule(mutableListOf<String>()),
            CappedEmissionFactorDraftRequiredLabelRule("capped emission factor draft needs a value"),
            CappedEmissionFactorDraftRequiredDriverIdRule("capped emission factor draft needs this field"),
        ),
    )
}
