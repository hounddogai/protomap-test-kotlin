package com.acme.transport.customs.declaration.notice

/** A rule a declaration entry must pass. Check returns its problems, or none. */
interface DeclarationEntryRule {
    fun check(item: DeclarationEntry): MutableList<String>
}

data class DeclarationEntryRequiredTitleRule(var message: String) : DeclarationEntryRule {
    override fun check(item: DeclarationEntry): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationEntryRequiredCodeRule(var message: String) : DeclarationEntryRule {
    override fun check(item: DeclarationEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationEntryCodeLengthRule(var maxLength: Int) : DeclarationEntryRule {
    override fun check(item: DeclarationEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration entry. */
data class DeclarationEntryValidator(var rules: MutableList<DeclarationEntryRule>) {
    fun validate(item: DeclarationEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DeclarationEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationEntryValidator(): DeclarationEntryValidator {
    return DeclarationEntryValidator(
        mutableListOf<DeclarationEntryRule>(
            DeclarationEntryRequiredTitleRule("declaration entry needs this field"),
            DeclarationEntryRequiredCodeRule("declaration entry needs an entry"),
            DeclarationEntryCodeLengthRule(16),
        ),
    )
}
