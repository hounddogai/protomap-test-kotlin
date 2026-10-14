package com.acme.transport.crewing.roster.agreement

/** A rule a default roster review must pass. Check returns its problems, or none. */
interface DefaultRosterReviewRule {
    fun check(item: DefaultRosterReview): MutableList<String>
}

data class DefaultRosterReviewRequiredShiftCodeRule(var message: String) : DefaultRosterReviewRule {
    override fun check(item: DefaultRosterReview): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRosterReviewShiftCodeLengthRule(var maxLength: Int) : DefaultRosterReviewRule {
    override fun check(item: DefaultRosterReview): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRosterReviewCodePrefixRule(var prefix: String) : DefaultRosterReviewRule {
    override fun check(item: DefaultRosterReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRosterReviewCodeLengthRule(var maxLength: Int) : DefaultRosterReviewRule {
    override fun check(item: DefaultRosterReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRosterReviewUpdatedAtRangeRule(var min: Int, var max: Int) : DefaultRosterReviewRule {
    override fun check(item: DefaultRosterReview): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default roster review. */
data class DefaultRosterReviewValidator(var rules: MutableList<DefaultRosterReviewRule>) {
    fun validate(item: DefaultRosterReview): MutableList<String> {
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

    fun requireValid(item: DefaultRosterReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRosterReview>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRosterReviewValidator(): DefaultRosterReviewValidator {
    return DefaultRosterReviewValidator(
        mutableListOf<DefaultRosterReviewRule>(
            DefaultRosterReviewRequiredShiftCodeRule("default roster review needs an entry"),
            DefaultRosterReviewShiftCodeLengthRule(120),
            DefaultRosterReviewCodePrefixRule("X"),
            DefaultRosterReviewCodeLengthRule(64),
            DefaultRosterReviewUpdatedAtRangeRule(120, 86400),
        ),
    )
}
