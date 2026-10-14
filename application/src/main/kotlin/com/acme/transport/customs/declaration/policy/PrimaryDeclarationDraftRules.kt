package com.acme.transport.customs.declaration.policy

import com.acme.transport.kit.text.normalize

/** A rule a primary declaration draft must pass. Check returns its problems, or none. */
interface PrimaryDeclarationDraftRule {
    fun check(item: PrimaryDeclarationDraft): MutableList<String>
}

data class PrimaryDeclarationDraftRequiredLabelRule(var message: String) : PrimaryDeclarationDraftRule {
    override fun check(item: PrimaryDeclarationDraft): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDeclarationDraftCodePrefixRule(var prefix: String) : PrimaryDeclarationDraftRule {
    override fun check(item: PrimaryDeclarationDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDeclarationDraftRequiredCodeRule(var message: String) : PrimaryDeclarationDraftRule {
    override fun check(item: PrimaryDeclarationDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDeclarationDraftRequiredExternalRefRule(var message: String) : PrimaryDeclarationDraftRule {
    override fun check(item: PrimaryDeclarationDraft): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDeclarationDraftCodeLengthRule(var maxLength: Int) : PrimaryDeclarationDraftRule {
    override fun check(item: PrimaryDeclarationDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDeclarationDraftAllowedLabelRule(
    var allowed: MutableList<String>,
) : PrimaryDeclarationDraftRule {
    override fun check(item: PrimaryDeclarationDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary declaration draft. */
data class PrimaryDeclarationDraftValidator(var rules: MutableList<PrimaryDeclarationDraftRule>) {
    fun validate(item: PrimaryDeclarationDraft): MutableList<String> {
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

    fun requireValid(item: PrimaryDeclarationDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryDeclarationDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryDeclarationDraftValidator(): PrimaryDeclarationDraftValidator {
    return PrimaryDeclarationDraftValidator(
        mutableListOf<PrimaryDeclarationDraftRule>(
            PrimaryDeclarationDraftRequiredLabelRule("primary declaration draft needs this field"),
            PrimaryDeclarationDraftCodePrefixRule("ACM"),
            PrimaryDeclarationDraftRequiredCodeRule("primary declaration draft needs this field"),
            PrimaryDeclarationDraftRequiredExternalRefRule("primary declaration draft needs a value"),
            PrimaryDeclarationDraftCodeLengthRule(80),
            PrimaryDeclarationDraftAllowedLabelRule(mutableListOf<String>("primary", "seasonal")),
        ),
    )
}
