package com.acme.transport.terminal.sortation.review

/** A rule a sortation exception must pass. Check returns its problems, or none. */
interface SortationExceptionRule {
    fun check(item: SortationException): MutableList<String>
}

data class SortationExceptionTitleLengthRule(var maxLength: Int) : SortationExceptionRule {
    override fun check(item: SortationException): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationExceptionCodePrefixRule(var prefix: String) : SortationExceptionRule {
    override fun check(item: SortationException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SortationExceptionNotesLengthRule(var maxLength: Int) : SortationExceptionRule {
    override fun check(item: SortationException): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationExceptionRequiredTitleRule(var message: String) : SortationExceptionRule {
    override fun check(item: SortationException): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationExceptionReviewerLengthRule(var maxLength: Int) : SortationExceptionRule {
    override fun check(item: SortationException): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation exception. */
data class SortationExceptionValidator(var rules: MutableList<SortationExceptionRule>) {
    fun validate(item: SortationException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SortationException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationException>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationExceptionValidator(): SortationExceptionValidator {
    return SortationExceptionValidator(
        mutableListOf<SortationExceptionRule>(
            SortationExceptionTitleLengthRule(120),
            SortationExceptionCodePrefixRule("REF"),
            SortationExceptionNotesLengthRule(3600),
            SortationExceptionRequiredTitleRule("sortation exception needs an entry"),
            SortationExceptionReviewerLengthRule(1000),
        ),
    )
}
