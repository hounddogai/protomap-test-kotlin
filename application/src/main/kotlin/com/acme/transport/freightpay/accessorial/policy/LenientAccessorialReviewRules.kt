package com.acme.transport.freightpay.accessorial.policy

/** A rule a lenient accessorial review must pass. Check returns its problems, or none. */
interface LenientAccessorialReviewRule {
    fun check(item: LenientAccessorialReview): MutableList<String>
}

data class LenientAccessorialReviewNameLengthRule(var maxLength: Int) : LenientAccessorialReviewRule {
    override fun check(item: LenientAccessorialReview): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientAccessorialReviewCodePrefixRule(var prefix: String) : LenientAccessorialReviewRule {
    override fun check(item: LenientAccessorialReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientAccessorialReviewRequiredLocaleRule(var message: String) : LenientAccessorialReviewRule {
    override fun check(item: LenientAccessorialReview): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientAccessorialReviewLocaleLengthRule(var maxLength: Int) : LenientAccessorialReviewRule {
    override fun check(item: LenientAccessorialReview): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient accessorial review. */
data class LenientAccessorialReviewValidator(var rules: MutableList<LenientAccessorialReviewRule>) {
    fun validate(item: LenientAccessorialReview): MutableList<String> {
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

    fun requireValid(item: LenientAccessorialReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientAccessorialReview>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientAccessorialReviewValidator(): LenientAccessorialReviewValidator {
    return LenientAccessorialReviewValidator(
        mutableListOf<LenientAccessorialReviewRule>(
            LenientAccessorialReviewNameLengthRule(64),
            LenientAccessorialReviewCodePrefixRule("REF"),
            LenientAccessorialReviewRequiredLocaleRule("lenient accessorial review needs this field"),
            LenientAccessorialReviewLocaleLengthRule(120),
        ),
    )
}
