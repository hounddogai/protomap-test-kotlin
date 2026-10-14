package com.acme.transport.terminal.sortation.strictsortationdigest

/** A rule a sortation quota must pass. Check returns its problems, or none. */
interface SortationQuotaRule {
    fun check(item: SortationQuota): MutableList<String>
}

data class SortationQuotaRequiredCategoryRule(var message: String) : SortationQuotaRule {
    override fun check(item: SortationQuota): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationQuotaCategoryLengthRule(var maxLength: Int) : SortationQuotaRule {
    override fun check(item: SortationQuota): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationQuotaRequiredExternalRefRule(var message: String) : SortationQuotaRule {
    override fun check(item: SortationQuota): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationQuotaCodePrefixRule(var prefix: String) : SortationQuotaRule {
    override fun check(item: SortationQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation quota. */
data class SortationQuotaValidator(var rules: MutableList<SortationQuotaRule>) {
    fun validate(item: SortationQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SortationQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationQuotaValidator(): SortationQuotaValidator {
    return SortationQuotaValidator(
        mutableListOf<SortationQuotaRule>(
            SortationQuotaRequiredCategoryRule("sortation quota needs a value"),
            SortationQuotaCategoryLengthRule(3600),
            SortationQuotaRequiredExternalRefRule("sortation quota needs this field"),
            SortationQuotaCodePrefixRule("X"),
        ),
    )
}
