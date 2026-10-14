package com.acme.transport.terminal.sortation.milestone

/** A rule a sortation window must pass. Check returns its problems, or none. */
interface SortationWindowRule {
    fun check(item: SortationWindow): MutableList<String>
}

data class SortationWindowRequiredTitleRule(var message: String) : SortationWindowRule {
    override fun check(item: SortationWindow): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationWindowCodePrefixRule(var prefix: String) : SortationWindowRule {
    override fun check(item: SortationWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SortationWindowChecksumLengthRule(var maxLength: Int) : SortationWindowRule {
    override fun check(item: SortationWindow): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationWindowCodeLengthRule(var maxLength: Int) : SortationWindowRule {
    override fun check(item: SortationWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationWindowRequiredExternalRefRule(var message: String) : SortationWindowRule {
    override fun check(item: SortationWindow): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationWindowUpdatedAtRangeRule(var min: Int, var max: Int) : SortationWindowRule {
    override fun check(item: SortationWindow): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation window. */
data class SortationWindowValidator(var rules: MutableList<SortationWindowRule>) {
    fun validate(item: SortationWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SortationWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationWindowValidator(): SortationWindowValidator {
    return SortationWindowValidator(
        mutableListOf<SortationWindowRule>(
            SortationWindowRequiredTitleRule("sortation window needs this field"),
            SortationWindowCodePrefixRule("ACM"),
            SortationWindowChecksumLengthRule(3600),
            SortationWindowCodeLengthRule(1000),
            SortationWindowRequiredExternalRefRule("sortation window needs this field"),
            SortationWindowUpdatedAtRangeRule(32, 1000),
        ),
    )
}
