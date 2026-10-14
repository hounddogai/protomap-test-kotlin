package com.acme.transport.lastmile.redelivery.redeliverywindow

/** A rule a tiered redelivery review must pass. Check returns its problems, or none. */
interface TieredRedeliveryReviewRule {
    fun check(item: TieredRedeliveryReview): MutableList<String>
}

data class TieredRedeliveryReviewRequiredOwnerIdRule(var message: String) : TieredRedeliveryReviewRule {
    override fun check(item: TieredRedeliveryReview): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredRedeliveryReviewVisibleGuardRule(var limit: Int) : TieredRedeliveryReviewRule {
    override fun check(item: TieredRedeliveryReview): MutableList<String> {
        if (item.visible && item.sequence > limit) {
            return mutableListOf<String>("sequence ${item.sequence} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class TieredRedeliveryReviewRequiredCodeRule(var message: String) : TieredRedeliveryReviewRule {
    override fun check(item: TieredRedeliveryReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered redelivery review. */
data class TieredRedeliveryReviewValidator(var rules: MutableList<TieredRedeliveryReviewRule>) {
    fun validate(item: TieredRedeliveryReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredRedeliveryReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredRedeliveryReview>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredRedeliveryReviewValidator(): TieredRedeliveryReviewValidator {
    return TieredRedeliveryReviewValidator(
        mutableListOf<TieredRedeliveryReviewRule>(
            TieredRedeliveryReviewRequiredOwnerIdRule("tiered redelivery review needs an entry"),
            TieredRedeliveryReviewVisibleGuardRule(64),
            TieredRedeliveryReviewRequiredCodeRule("tiered redelivery review needs a value"),
        ),
    )
}
