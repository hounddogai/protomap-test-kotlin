package com.acme.transport.customs.declaration.overrideitem

/** A rule a weighted declaration entry must pass. Check returns its problems, or none. */
interface WeightedDeclarationEntryRule {
    fun check(item: WeightedDeclarationEntry): MutableList<String>
}

data class WeightedDeclarationEntryRequiredCodeRule(var message: String) : WeightedDeclarationEntryRule {
    override fun check(item: WeightedDeclarationEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDeclarationEntryCategoryLengthRule(var maxLength: Int) : WeightedDeclarationEntryRule {
    override fun check(item: WeightedDeclarationEntry): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDeclarationEntryCodePrefixRule(var prefix: String) : WeightedDeclarationEntryRule {
    override fun check(item: WeightedDeclarationEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted declaration entry. */
data class WeightedDeclarationEntryValidator(var rules: MutableList<WeightedDeclarationEntryRule>) {
    fun validate(item: WeightedDeclarationEntry): MutableList<String> {
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

    fun requireValid(item: WeightedDeclarationEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedDeclarationEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedDeclarationEntryValidator(): WeightedDeclarationEntryValidator {
    return WeightedDeclarationEntryValidator(
        mutableListOf<WeightedDeclarationEntryRule>(
            WeightedDeclarationEntryRequiredCodeRule("weighted declaration entry needs this field"),
            WeightedDeclarationEntryCategoryLengthRule(3600),
            WeightedDeclarationEntryCodePrefixRule("ACM"),
        ),
    )
}
