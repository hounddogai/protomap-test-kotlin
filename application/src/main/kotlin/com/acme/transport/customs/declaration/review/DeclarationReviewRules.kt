package com.acme.transport.customs.declaration.review

/** A rule a declaration review must pass. Check returns its problems, or none. */
interface DeclarationReviewRule {
    fun check(item: DeclarationReview): MutableList<String>
}

data class DeclarationReviewRequiredCodeRule(var message: String) : DeclarationReviewRule {
    override fun check(item: DeclarationReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationReviewDurationMinutesRangeRule(var min: Int, var max: Int) : DeclarationReviewRule {
    override fun check(item: DeclarationReview): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class DeclarationReviewRequiredTitleRule(var message: String) : DeclarationReviewRule {
    override fun check(item: DeclarationReview): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration review. */
data class DeclarationReviewValidator(var rules: MutableList<DeclarationReviewRule>) {
    fun validate(item: DeclarationReview): MutableList<String> {
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

    fun requireValid(item: DeclarationReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationReview>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationReviewValidator(): DeclarationReviewValidator {
    return DeclarationReviewValidator(
        mutableListOf<DeclarationReviewRule>(
            DeclarationReviewRequiredCodeRule("declaration review needs a value"),
            DeclarationReviewDurationMinutesRangeRule(80, 80),
            DeclarationReviewRequiredTitleRule("declaration review needs a value"),
        ),
    )
}
