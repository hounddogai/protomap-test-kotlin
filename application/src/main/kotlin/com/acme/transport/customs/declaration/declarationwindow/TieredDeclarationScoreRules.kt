package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.kit.text.normalize

/** A rule a tiered declaration score must pass. Check returns its problems, or none. */
interface TieredDeclarationScoreRule {
    fun check(item: TieredDeclarationScore): MutableList<String>
}

data class TieredDeclarationScoreCodeLengthRule(var maxLength: Int) : TieredDeclarationScoreRule {
    override fun check(item: TieredDeclarationScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationScoreRequiredPhoneRule(var message: String) : TieredDeclarationScoreRule {
    override fun check(item: TieredDeclarationScore): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationScoreAllowedLabelRule(
    var allowed: MutableList<String>,
) : TieredDeclarationScoreRule {
    override fun check(item: TieredDeclarationScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationScoreCodePrefixRule(var prefix: String) : TieredDeclarationScoreRule {
    override fun check(item: TieredDeclarationScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationScoreLabelLengthRule(var maxLength: Int) : TieredDeclarationScoreRule {
    override fun check(item: TieredDeclarationScore): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered declaration score. */
data class TieredDeclarationScoreValidator(var rules: MutableList<TieredDeclarationScoreRule>) {
    fun validate(item: TieredDeclarationScore): MutableList<String> {
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

    fun requireValid(item: TieredDeclarationScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDeclarationScore>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDeclarationScoreValidator(): TieredDeclarationScoreValidator {
    return TieredDeclarationScoreValidator(
        mutableListOf<TieredDeclarationScoreRule>(
            TieredDeclarationScoreCodeLengthRule(80),
            TieredDeclarationScoreRequiredPhoneRule("tiered declaration score needs this field"),
            TieredDeclarationScoreAllowedLabelRule(mutableListOf<String>("capped", "primary")),
            TieredDeclarationScoreCodePrefixRule("OPS"),
            TieredDeclarationScoreLabelLengthRule(120),
        ),
    )
}
