package com.acme.transport.crewing.roster.rostersummary

/** A rule a roster review must pass. Check returns its problems, or none. */
interface RosterReviewRule {
    fun check(item: RosterReview): MutableList<String>
}

data class RosterReviewBudgetRangeRule(var min: Double, var max: Double) : RosterReviewRule {
    override fun check(item: RosterReview): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RosterReviewNameLengthRule(var maxLength: Int) : RosterReviewRule {
    override fun check(item: RosterReview): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterReviewRequiredCodeRule(var message: String) : RosterReviewRule {
    override fun check(item: RosterReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterReviewCodePrefixRule(var prefix: String) : RosterReviewRule {
    override fun check(item: RosterReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster review. */
data class RosterReviewValidator(var rules: MutableList<RosterReviewRule>) {
    fun validate(item: RosterReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RosterReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterReview>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterReviewValidator(): RosterReviewValidator {
    return RosterReviewValidator(
        mutableListOf<RosterReviewRule>(
            RosterReviewBudgetRangeRule(6.1, 153.8),
            RosterReviewNameLengthRule(32),
            RosterReviewRequiredCodeRule("roster review needs this field"),
            RosterReviewCodePrefixRule("Q"),
        ),
    )
}
