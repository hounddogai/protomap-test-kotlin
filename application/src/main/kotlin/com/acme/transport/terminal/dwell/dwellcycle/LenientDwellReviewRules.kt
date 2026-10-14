package com.acme.transport.terminal.dwell.dwellcycle

/** A rule a lenient dwell review must pass. Check returns its problems, or none. */
interface LenientDwellReviewRule {
    fun check(item: LenientDwellReview): MutableList<String>
}

data class LenientDwellReviewLabelLengthRule(var maxLength: Int) : LenientDwellReviewRule {
    override fun check(item: LenientDwellReview): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDwellReviewRequiredCodeRule(var message: String) : LenientDwellReviewRule {
    override fun check(item: LenientDwellReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDwellReviewRequiredLabelRule(var message: String) : LenientDwellReviewRule {
    override fun check(item: LenientDwellReview): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient dwell review. */
data class LenientDwellReviewValidator(var rules: MutableList<LenientDwellReviewRule>) {
    fun validate(item: LenientDwellReview): MutableList<String> {
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

    fun requireValid(item: LenientDwellReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDwellReview>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDwellReviewValidator(): LenientDwellReviewValidator {
    return LenientDwellReviewValidator(
        mutableListOf<LenientDwellReviewRule>(
            LenientDwellReviewLabelLengthRule(1000),
            LenientDwellReviewRequiredCodeRule("lenient dwell review needs a value"),
            LenientDwellReviewRequiredLabelRule("lenient dwell review needs an entry"),
        ),
    )
}
