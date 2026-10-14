package com.acme.transport.crewing.swap.entry

/** A rule a regional swap entry must pass. Check returns its problems, or none. */
interface RegionalSwapEntryRule {
    fun check(item: RegionalSwapEntry): MutableList<String>
}

data class RegionalSwapEntryPinnedGuardRule(var limit: Double) : RegionalSwapEntryRule {
    override fun check(item: RegionalSwapEntry): MutableList<String> {
        if (item.pinned && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class RegionalSwapEntryCodePrefixRule(var prefix: String) : RegionalSwapEntryRule {
    override fun check(item: RegionalSwapEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalSwapEntryRequiredTitleRule(var message: String) : RegionalSwapEntryRule {
    override fun check(item: RegionalSwapEntry): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalSwapEntryExternalRefLengthRule(var maxLength: Int) : RegionalSwapEntryRule {
    override fun check(item: RegionalSwapEntry): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalSwapEntryMinimumRangeRule(var min: Double, var max: Double) : RegionalSwapEntryRule {
    override fun check(item: RegionalSwapEntry): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional swap entry. */
data class RegionalSwapEntryValidator(var rules: MutableList<RegionalSwapEntryRule>) {
    fun validate(item: RegionalSwapEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalSwapEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalSwapEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalSwapEntryValidator(): RegionalSwapEntryValidator {
    return RegionalSwapEntryValidator(
        mutableListOf<RegionalSwapEntryRule>(
            RegionalSwapEntryPinnedGuardRule(5.1),
            RegionalSwapEntryCodePrefixRule("Q"),
            RegionalSwapEntryRequiredTitleRule("regional swap entry needs this field"),
            RegionalSwapEntryExternalRefLengthRule(3600),
            RegionalSwapEntryMinimumRangeRule(9.5, 2090.9),
        ),
    )
}
