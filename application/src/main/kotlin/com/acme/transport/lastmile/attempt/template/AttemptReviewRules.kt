package com.acme.transport.lastmile.attempt.template

/** A rule a attempt review must pass. Check returns its problems, or none. */
interface AttemptReviewRule {
    fun check(item: AttemptReview): MutableList<String>
}

data class AttemptReviewCodePrefixRule(var prefix: String) : AttemptReviewRule {
    override fun check(item: AttemptReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AttemptReviewCeilingRangeRule(var min: Double, var max: Double) : AttemptReviewRule {
    override fun check(item: AttemptReview): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class AttemptReviewRequiredNameRule(var message: String) : AttemptReviewRule {
    override fun check(item: AttemptReview): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptReviewRequiredCodeRule(var message: String) : AttemptReviewRule {
    override fun check(item: AttemptReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt review. */
data class AttemptReviewValidator(var rules: MutableList<AttemptReviewRule>) {
    fun validate(item: AttemptReview): MutableList<String> {
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

    fun requireValid(item: AttemptReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptReview>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptReviewValidator(): AttemptReviewValidator {
    return AttemptReviewValidator(
        mutableListOf<AttemptReviewRule>(
            AttemptReviewCodePrefixRule("REF"),
            AttemptReviewCeilingRangeRule(3.5, 108.2),
            AttemptReviewRequiredNameRule("attempt review needs this field"),
            AttemptReviewRequiredCodeRule("attempt review needs this field"),
        ),
    )
}
