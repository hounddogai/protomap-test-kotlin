package com.acme.transport.linehaul.trailer.draft

import com.acme.transport.kit.text.normalize

/** A rule a trailer draft must pass. Check returns its problems, or none. */
interface TrailerDraftRule {
    fun check(item: TrailerDraft): MutableList<String>
}

data class TrailerDraftAllowedNameRule(var allowed: MutableList<String>) : TrailerDraftRule {
    override fun check(item: TrailerDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class TrailerDraftCodeLengthRule(var maxLength: Int) : TrailerDraftRule {
    override fun check(item: TrailerDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerDraftNameLengthRule(var maxLength: Int) : TrailerDraftRule {
    override fun check(item: TrailerDraft): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerDraftScoreRangeRule(var min: Double, var max: Double) : TrailerDraftRule {
    override fun check(item: TrailerDraft): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class TrailerDraftRequiredTitleRule(var message: String) : TrailerDraftRule {
    override fun check(item: TrailerDraft): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerDraftCodePrefixRule(var prefix: String) : TrailerDraftRule {
    override fun check(item: TrailerDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer draft. */
data class TrailerDraftValidator(var rules: MutableList<TrailerDraftRule>) {
    fun validate(item: TrailerDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerDraftValidator(): TrailerDraftValidator {
    return TrailerDraftValidator(
        mutableListOf<TrailerDraftRule>(
            TrailerDraftAllowedNameRule(mutableListOf<String>("legacy", "fallback", "pending")),
            TrailerDraftCodeLengthRule(255),
            TrailerDraftNameLengthRule(120),
            TrailerDraftScoreRangeRule(1.2, 2334.2),
            TrailerDraftRequiredTitleRule("trailer draft needs this field"),
            TrailerDraftCodePrefixRule("REF"),
        ),
    )
}
