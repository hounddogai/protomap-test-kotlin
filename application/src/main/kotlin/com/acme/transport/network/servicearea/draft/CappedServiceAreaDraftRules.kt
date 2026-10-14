package com.acme.transport.network.servicearea.draft

import com.acme.transport.kit.text.normalize

/** A rule a capped service area draft must pass. Check returns its problems, or none. */
interface CappedServiceAreaDraftRule {
    fun check(item: CappedServiceAreaDraft): MutableList<String>
}

data class CappedServiceAreaDraftAllowedLabelRule(
    var allowed: MutableList<String>,
) : CappedServiceAreaDraftRule {
    override fun check(item: CappedServiceAreaDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class CappedServiceAreaDraftCarrierLengthRule(var maxLength: Int) : CappedServiceAreaDraftRule {
    override fun check(item: CappedServiceAreaDraft): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedServiceAreaDraftRequiredLabelRule(var message: String) : CappedServiceAreaDraftRule {
    override fun check(item: CappedServiceAreaDraft): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedServiceAreaDraftDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : CappedServiceAreaDraftRule {
    override fun check(item: CappedServiceAreaDraft): MutableList<String> {
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

/** Runs every rule of a capped service area draft. */
data class CappedServiceAreaDraftValidator(var rules: MutableList<CappedServiceAreaDraftRule>) {
    fun validate(item: CappedServiceAreaDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedServiceAreaDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedServiceAreaDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedServiceAreaDraftValidator(): CappedServiceAreaDraftValidator {
    return CappedServiceAreaDraftValidator(
        mutableListOf<CappedServiceAreaDraftRule>(
            CappedServiceAreaDraftAllowedLabelRule(mutableListOf<String>("regional", "regional", "seasonal")),
            CappedServiceAreaDraftCarrierLengthRule(120),
            CappedServiceAreaDraftRequiredLabelRule("capped service area draft needs a value"),
            CappedServiceAreaDraftDurationMinutesRangeRule(32, 86400),
        ),
    )
}
