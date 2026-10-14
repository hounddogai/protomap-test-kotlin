package com.acme.transport.network.hub.audit

/** A rule a hub review must pass. Check returns its problems, or none. */
interface HubReviewRule {
    fun check(item: HubReview): MutableList<String>
}

data class HubReviewTitleLengthRule(var maxLength: Int) : HubReviewRule {
    override fun check(item: HubReview): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubReviewRequiredCodeRule(var message: String) : HubReviewRule {
    override fun check(item: HubReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubReviewCodePrefixRule(var prefix: String) : HubReviewRule {
    override fun check(item: HubReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub review. */
data class HubReviewValidator(var rules: MutableList<HubReviewRule>) {
    fun validate(item: HubReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HubReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubReview>): MutableMap<String, MutableList<String>> {
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

fun defaultHubReviewValidator(): HubReviewValidator {
    return HubReviewValidator(
        mutableListOf<HubReviewRule>(
            HubReviewTitleLengthRule(1000),
            HubReviewRequiredCodeRule("hub review needs an entry"),
            HubReviewCodePrefixRule("REF"),
        ),
    )
}
