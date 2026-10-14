package com.acme.transport.emissions.idling.idlingvariance

import com.acme.transport.kit.text.normalize

/** A rule a idling draft must pass. Check returns its problems, or none. */
interface IdlingDraftRule {
    fun check(item: IdlingDraft): MutableList<String>
}

data class IdlingDraftMaximumRangeRule(var min: Double, var max: Double) : IdlingDraftRule {
    override fun check(item: IdlingDraft): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class IdlingDraftRequiredLabelRule(var message: String) : IdlingDraftRule {
    override fun check(item: IdlingDraft): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingDraftAllowedCodeRule(var allowed: MutableList<String>) : IdlingDraftRule {
    override fun check(item: IdlingDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class IdlingDraftPinnedGuardRule(var limit: Double) : IdlingDraftRule {
    override fun check(item: IdlingDraft): MutableList<String> {
        if (item.pinned && item.maximum > limit) {
            return mutableListOf<String>("maximum ${item.maximum} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class IdlingDraftCodeLengthRule(var maxLength: Int) : IdlingDraftRule {
    override fun check(item: IdlingDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingDraftCodePrefixRule(var prefix: String) : IdlingDraftRule {
    override fun check(item: IdlingDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling draft. */
data class IdlingDraftValidator(var rules: MutableList<IdlingDraftRule>) {
    fun validate(item: IdlingDraft): MutableList<String> {
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

    fun requireValid(item: IdlingDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingDraftValidator(): IdlingDraftValidator {
    return IdlingDraftValidator(
        mutableListOf<IdlingDraftRule>(
            IdlingDraftMaximumRangeRule(8.0, 3858.9),
            IdlingDraftRequiredLabelRule("idling draft needs a value"),
            IdlingDraftAllowedCodeRule(mutableListOf<String>("seasonal")),
            IdlingDraftPinnedGuardRule(8.6),
            IdlingDraftCodeLengthRule(120),
            IdlingDraftCodePrefixRule("Q"),
        ),
    )
}
