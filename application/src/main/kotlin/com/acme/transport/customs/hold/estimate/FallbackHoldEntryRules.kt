package com.acme.transport.customs.hold.estimate

/** A rule a fallback hold entry must pass. Check returns its problems, or none. */
interface FallbackHoldEntryRule {
    fun check(item: FallbackHoldEntry): MutableList<String>
}

data class FallbackHoldEntryTitleLengthRule(var maxLength: Int) : FallbackHoldEntryRule {
    override fun check(item: FallbackHoldEntry): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackHoldEntryCodePrefixRule(var prefix: String) : FallbackHoldEntryRule {
    override fun check(item: FallbackHoldEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackHoldEntryRequiredCategoryRule(var message: String) : FallbackHoldEntryRule {
    override fun check(item: FallbackHoldEntry): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackHoldEntryCategoryLengthRule(var maxLength: Int) : FallbackHoldEntryRule {
    override fun check(item: FallbackHoldEntry): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback hold entry. */
data class FallbackHoldEntryValidator(var rules: MutableList<FallbackHoldEntryRule>) {
    fun validate(item: FallbackHoldEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackHoldEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackHoldEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackHoldEntryValidator(): FallbackHoldEntryValidator {
    return FallbackHoldEntryValidator(
        mutableListOf<FallbackHoldEntryRule>(
            FallbackHoldEntryTitleLengthRule(16),
            FallbackHoldEntryCodePrefixRule("X"),
            FallbackHoldEntryRequiredCategoryRule("fallback hold entry needs an entry"),
            FallbackHoldEntryCategoryLengthRule(32),
        ),
    )
}
