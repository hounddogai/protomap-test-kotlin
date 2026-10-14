package com.acme.transport.terminal.sortation.window

/** A rule a default sortation review must pass. Check returns its problems, or none. */
interface DefaultSortationReviewRule {
    fun check(item: DefaultSortationReview): MutableList<String>
}

data class DefaultSortationReviewDepotLengthRule(var maxLength: Int) : DefaultSortationReviewRule {
    override fun check(item: DefaultSortationReview): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultSortationReviewCodePrefixRule(var prefix: String) : DefaultSortationReviewRule {
    override fun check(item: DefaultSortationReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultSortationReviewRequiredDepotRule(var message: String) : DefaultSortationReviewRule {
    override fun check(item: DefaultSortationReview): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default sortation review. */
data class DefaultSortationReviewValidator(var rules: MutableList<DefaultSortationReviewRule>) {
    fun validate(item: DefaultSortationReview): MutableList<String> {
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

    fun requireValid(item: DefaultSortationReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultSortationReview>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultSortationReviewValidator(): DefaultSortationReviewValidator {
    return DefaultSortationReviewValidator(
        mutableListOf<DefaultSortationReviewRule>(
            DefaultSortationReviewDepotLengthRule(1000),
            DefaultSortationReviewCodePrefixRule("X"),
            DefaultSortationReviewRequiredDepotRule("default sortation review needs a value"),
        ),
    )
}
