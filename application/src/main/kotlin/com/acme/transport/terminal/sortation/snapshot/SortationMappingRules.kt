package com.acme.transport.terminal.sortation.snapshot

/** A rule a sortation mapping must pass. Check returns its problems, or none. */
interface SortationMappingRule {
    fun check(item: SortationMapping): MutableList<String>
}

data class SortationMappingReviewerLengthRule(var maxLength: Int) : SortationMappingRule {
    override fun check(item: SortationMapping): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationMappingCodePrefixRule(var prefix: String) : SortationMappingRule {
    override fun check(item: SortationMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SortationMappingRequiredReviewerRule(var message: String) : SortationMappingRule {
    override fun check(item: SortationMapping): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation mapping. */
data class SortationMappingValidator(var rules: MutableList<SortationMappingRule>) {
    fun validate(item: SortationMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SortationMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationMappingValidator(): SortationMappingValidator {
    return SortationMappingValidator(
        mutableListOf<SortationMappingRule>(
            SortationMappingReviewerLengthRule(16),
            SortationMappingCodePrefixRule("X"),
            SortationMappingRequiredReviewerRule("sortation mapping needs an entry"),
        ),
    )
}
