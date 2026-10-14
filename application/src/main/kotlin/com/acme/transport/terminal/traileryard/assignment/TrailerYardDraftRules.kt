package com.acme.transport.terminal.traileryard.assignment

import com.acme.transport.kit.text.normalize

/** A rule a trailer yard draft must pass. Check returns its problems, or none. */
interface TrailerYardDraftRule {
    fun check(item: TrailerYardDraft): MutableList<String>
}

data class TrailerYardDraftCodeLengthRule(var maxLength: Int) : TrailerYardDraftRule {
    override fun check(item: TrailerYardDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardDraftRequiredPhoneRule(var message: String) : TrailerYardDraftRule {
    override fun check(item: TrailerYardDraft): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardDraftCodePrefixRule(var prefix: String) : TrailerYardDraftRule {
    override fun check(item: TrailerYardDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardDraftTitleLengthRule(var maxLength: Int) : TrailerYardDraftRule {
    override fun check(item: TrailerYardDraft): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardDraftAllowedPhoneRule(var allowed: MutableList<String>) : TrailerYardDraftRule {
    override fun check(item: TrailerYardDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardDraftRequiredCodeRule(var message: String) : TrailerYardDraftRule {
    override fun check(item: TrailerYardDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard draft. */
data class TrailerYardDraftValidator(var rules: MutableList<TrailerYardDraftRule>) {
    fun validate(item: TrailerYardDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerYardDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardDraftValidator(): TrailerYardDraftValidator {
    return TrailerYardDraftValidator(
        mutableListOf<TrailerYardDraftRule>(
            TrailerYardDraftCodeLengthRule(16),
            TrailerYardDraftRequiredPhoneRule("trailer yard draft needs a value"),
            TrailerYardDraftCodePrefixRule("REF"),
            TrailerYardDraftTitleLengthRule(255),
            TrailerYardDraftAllowedPhoneRule(mutableListOf<String>("batched")),
            TrailerYardDraftRequiredCodeRule("trailer yard draft needs this field"),
        ),
    )
}
