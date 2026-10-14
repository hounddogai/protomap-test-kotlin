package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.kit.text.normalize

/** A rule a primary trailer yard draft must pass. Check returns its problems, or none. */
interface PrimaryTrailerYardDraftRule {
    fun check(item: PrimaryTrailerYardDraft): MutableList<String>
}

data class PrimaryTrailerYardDraftAllowedLabelRule(
    var allowed: MutableList<String>,
) : PrimaryTrailerYardDraftRule {
    override fun check(item: PrimaryTrailerYardDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is stale")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTrailerYardDraftEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryTrailerYardDraftRule {
    override fun check(item: PrimaryTrailerYardDraft): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class PrimaryTrailerYardDraftRequiredCodeRule(var message: String) : PrimaryTrailerYardDraftRule {
    override fun check(item: PrimaryTrailerYardDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary trailer yard draft. */
data class PrimaryTrailerYardDraftValidator(var rules: MutableList<PrimaryTrailerYardDraftRule>) {
    fun validate(item: PrimaryTrailerYardDraft): MutableList<String> {
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

    fun requireValid(item: PrimaryTrailerYardDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTrailerYardDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTrailerYardDraftValidator(): PrimaryTrailerYardDraftValidator {
    return PrimaryTrailerYardDraftValidator(
        mutableListOf<PrimaryTrailerYardDraftRule>(
            PrimaryTrailerYardDraftAllowedLabelRule(mutableListOf<String>("legacy", "seasonal")),
            PrimaryTrailerYardDraftEtaMinutesRangeRule(64, 1000),
            PrimaryTrailerYardDraftRequiredCodeRule("primary trailer yard draft needs a value"),
        ),
    )
}
