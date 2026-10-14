package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.kit.text.normalize

/** A rule a tiered declaration window must pass. Check returns its problems, or none. */
interface TieredDeclarationWindowRule {
    fun check(item: TieredDeclarationWindow): MutableList<String>
}

data class TieredDeclarationWindowAllowedTitleRule(
    var allowed: MutableList<String>,
) : TieredDeclarationWindowRule {
    override fun check(item: TieredDeclarationWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationWindowRequiredCodeRule(var message: String) : TieredDeclarationWindowRule {
    override fun check(item: TieredDeclarationWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationWindowRequiredNotesRule(var message: String) : TieredDeclarationWindowRule {
    override fun check(item: TieredDeclarationWindow): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationWindowCodePrefixRule(var prefix: String) : TieredDeclarationWindowRule {
    override fun check(item: TieredDeclarationWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationWindowRequiredDescriptionRule(var message: String) : TieredDeclarationWindowRule {
    override fun check(item: TieredDeclarationWindow): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationWindowPriorityRangeRule(var min: Int, var max: Int) : TieredDeclarationWindowRule {
    override fun check(item: TieredDeclarationWindow): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tiered declaration window. */
data class TieredDeclarationWindowValidator(var rules: MutableList<TieredDeclarationWindowRule>) {
    fun validate(item: TieredDeclarationWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredDeclarationWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDeclarationWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDeclarationWindowValidator(): TieredDeclarationWindowValidator {
    return TieredDeclarationWindowValidator(
        mutableListOf<TieredDeclarationWindowRule>(
            TieredDeclarationWindowAllowedTitleRule(mutableListOf<String>("default", "pending")),
            TieredDeclarationWindowRequiredCodeRule("tiered declaration window needs this field"),
            TieredDeclarationWindowRequiredNotesRule("tiered declaration window needs an entry"),
            TieredDeclarationWindowCodePrefixRule("Q"),
            TieredDeclarationWindowRequiredDescriptionRule("tiered declaration window needs an entry"),
            TieredDeclarationWindowPriorityRangeRule(120, 1000),
        ),
    )
}
