package com.acme.transport.network.zone.review

/** A rule a global zone review must pass. Check returns its problems, or none. */
interface GlobalZoneReviewRule {
    fun check(item: GlobalZoneReview): MutableList<String>
}

data class GlobalZoneReviewCountryLengthRule(var maxLength: Int) : GlobalZoneReviewRule {
    override fun check(item: GlobalZoneReview): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalZoneReviewTitleLengthRule(var maxLength: Int) : GlobalZoneReviewRule {
    override fun check(item: GlobalZoneReview): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalZoneReviewCodePrefixRule(var prefix: String) : GlobalZoneReviewRule {
    override fun check(item: GlobalZoneReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalZoneReviewDurationMinutesRangeRule(var min: Int, var max: Int) : GlobalZoneReviewRule {
    override fun check(item: GlobalZoneReview): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalZoneReviewRequiredExternalRefRule(var message: String) : GlobalZoneReviewRule {
    override fun check(item: GlobalZoneReview): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global zone review. */
data class GlobalZoneReviewValidator(var rules: MutableList<GlobalZoneReviewRule>) {
    fun validate(item: GlobalZoneReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalZoneReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalZoneReview>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalZoneReviewValidator(): GlobalZoneReviewValidator {
    return GlobalZoneReviewValidator(
        mutableListOf<GlobalZoneReviewRule>(
            GlobalZoneReviewCountryLengthRule(255),
            GlobalZoneReviewTitleLengthRule(120),
            GlobalZoneReviewCodePrefixRule("ACM"),
            GlobalZoneReviewDurationMinutesRangeRule(16, 1000),
            GlobalZoneReviewRequiredExternalRefRule("global zone review needs an entry"),
        ),
    )
}
