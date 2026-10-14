package com.acme.transport.linehaul.laneplan.item

/** A rule a fallback lane plan review must pass. Check returns its problems, or none. */
interface FallbackLanePlanReviewRule {
    fun check(item: FallbackLanePlanReview): MutableList<String>
}

data class FallbackLanePlanReviewRequiredCodeRule(var message: String) : FallbackLanePlanReviewRule {
    override fun check(item: FallbackLanePlanReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackLanePlanReviewRequiredCurrencyRule(var message: String) : FallbackLanePlanReviewRule {
    override fun check(item: FallbackLanePlanReview): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackLanePlanReviewActiveGuardRule(var limit: Int) : FallbackLanePlanReviewRule {
    override fun check(item: FallbackLanePlanReview): MutableList<String> {
        if (item.active && item.priority > limit) {
            return mutableListOf<String>("priority ${item.priority} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLanePlanReviewCurrencyLengthRule(var maxLength: Int) : FallbackLanePlanReviewRule {
    override fun check(item: FallbackLanePlanReview): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLanePlanReviewRequiredLabelRule(var message: String) : FallbackLanePlanReviewRule {
    override fun check(item: FallbackLanePlanReview): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback lane plan review. */
data class FallbackLanePlanReviewValidator(var rules: MutableList<FallbackLanePlanReviewRule>) {
    fun validate(item: FallbackLanePlanReview): MutableList<String> {
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

    fun requireValid(item: FallbackLanePlanReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackLanePlanReview>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackLanePlanReviewValidator(): FallbackLanePlanReviewValidator {
    return FallbackLanePlanReviewValidator(
        mutableListOf<FallbackLanePlanReviewRule>(
            FallbackLanePlanReviewRequiredCodeRule("fallback lane plan review needs a value"),
            FallbackLanePlanReviewRequiredCurrencyRule("fallback lane plan review needs this field"),
            FallbackLanePlanReviewActiveGuardRule(120),
            FallbackLanePlanReviewCurrencyLengthRule(86400),
            FallbackLanePlanReviewRequiredLabelRule("fallback lane plan review needs this field"),
        ),
    )
}
