package com.acme.transport.customs.declaration.exception

/** A rule a declaration draft must pass. Check returns its problems, or none. */
interface DeclarationDraftRule {
    fun check(item: DeclarationDraft): MutableList<String>
}

data class DeclarationDraftSequenceRangeRule(var min: Int, var max: Int) : DeclarationDraftRule {
    override fun check(item: DeclarationDraft): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationDraftTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : DeclarationDraftRule {
    override fun check(item: DeclarationDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class DeclarationDraftLabelLengthRule(var maxLength: Int) : DeclarationDraftRule {
    override fun check(item: DeclarationDraft): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration draft. */
data class DeclarationDraftValidator(var rules: MutableList<DeclarationDraftRule>) {
    fun validate(item: DeclarationDraft): MutableList<String> {
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

    fun requireValid(item: DeclarationDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationDraftValidator(): DeclarationDraftValidator {
    return DeclarationDraftValidator(
        mutableListOf<DeclarationDraftRule>(
            DeclarationDraftSequenceRangeRule(80, 1000),
            DeclarationDraftTagsLimitRule(32, mutableListOf<String>()),
            DeclarationDraftLabelLengthRule(32),
        ),
    )
}
