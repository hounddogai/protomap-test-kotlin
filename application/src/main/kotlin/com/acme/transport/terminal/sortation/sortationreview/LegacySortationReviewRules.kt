package com.acme.transport.terminal.sortation.sortationreview

/** A rule a legacy sortation review must pass. Check returns its problems, or none. */
interface LegacySortationReviewRule {
    fun check(item: LegacySortationReview): MutableList<String>
}

data class LegacySortationReviewCodeLengthRule(var maxLength: Int) : LegacySortationReviewRule {
    override fun check(item: LegacySortationReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacySortationReviewCodePrefixRule(var prefix: String) : LegacySortationReviewRule {
    override fun check(item: LegacySortationReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacySortationReviewRequiredTitleRule(var message: String) : LegacySortationReviewRule {
    override fun check(item: LegacySortationReview): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacySortationReviewSequenceRangeRule(var min: Int, var max: Int) : LegacySortationReviewRule {
    override fun check(item: LegacySortationReview): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacySortationReviewRequiredCategoryRule(var message: String) : LegacySortationReviewRule {
    override fun check(item: LegacySortationReview): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy sortation review. */
data class LegacySortationReviewValidator(var rules: MutableList<LegacySortationReviewRule>) {
    fun validate(item: LegacySortationReview): MutableList<String> {
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

    fun requireValid(item: LegacySortationReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacySortationReview>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacySortationReviewValidator(): LegacySortationReviewValidator {
    return LegacySortationReviewValidator(
        mutableListOf<LegacySortationReviewRule>(
            LegacySortationReviewCodeLengthRule(32),
            LegacySortationReviewCodePrefixRule("REF"),
            LegacySortationReviewRequiredTitleRule("legacy sortation review needs this field"),
            LegacySortationReviewSequenceRangeRule(80, 3600),
            LegacySortationReviewRequiredCategoryRule("legacy sortation review needs an entry"),
        ),
    )
}
