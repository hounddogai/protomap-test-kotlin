package com.acme.transport.crewing.restbreak.variance

/** A rule a lenient rest break review must pass. Check returns its problems, or none. */
interface LenientRestBreakReviewRule {
    fun check(item: LenientRestBreakReview): MutableList<String>
}

data class LenientRestBreakReviewPhoneLengthRule(var maxLength: Int) : LenientRestBreakReviewRule {
    override fun check(item: LenientRestBreakReview): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRestBreakReviewNameLengthRule(var maxLength: Int) : LenientRestBreakReviewRule {
    override fun check(item: LenientRestBreakReview): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRestBreakReviewCodePrefixRule(var prefix: String) : LenientRestBreakReviewRule {
    override fun check(item: LenientRestBreakReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient rest break review. */
data class LenientRestBreakReviewValidator(var rules: MutableList<LenientRestBreakReviewRule>) {
    fun validate(item: LenientRestBreakReview): MutableList<String> {
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

    fun requireValid(item: LenientRestBreakReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRestBreakReview>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRestBreakReviewValidator(): LenientRestBreakReviewValidator {
    return LenientRestBreakReviewValidator(
        mutableListOf<LenientRestBreakReviewRule>(
            LenientRestBreakReviewPhoneLengthRule(1000),
            LenientRestBreakReviewNameLengthRule(16),
            LenientRestBreakReviewCodePrefixRule("X"),
        ),
    )
}
