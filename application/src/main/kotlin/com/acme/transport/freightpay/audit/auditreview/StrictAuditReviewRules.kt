package com.acme.transport.freightpay.audit.auditreview

/** A rule a strict audit review must pass. Check returns its problems, or none. */
interface StrictAuditReviewRule {
    fun check(item: StrictAuditReview): MutableList<String>
}

data class StrictAuditReviewCodePrefixRule(var prefix: String) : StrictAuditReviewRule {
    override fun check(item: StrictAuditReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictAuditReviewRequiredRouteCodeRule(var message: String) : StrictAuditReviewRule {
    override fun check(item: StrictAuditReview): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictAuditReviewWindowDaysRangeRule(var min: Int, var max: Int) : StrictAuditReviewRule {
    override fun check(item: StrictAuditReview): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a strict audit review. */
data class StrictAuditReviewValidator(var rules: MutableList<StrictAuditReviewRule>) {
    fun validate(item: StrictAuditReview): MutableList<String> {
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

    fun requireValid(item: StrictAuditReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictAuditReview>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictAuditReviewValidator(): StrictAuditReviewValidator {
    return StrictAuditReviewValidator(
        mutableListOf<StrictAuditReviewRule>(
            StrictAuditReviewCodePrefixRule("ACM"),
            StrictAuditReviewRequiredRouteCodeRule("strict audit review needs a value"),
            StrictAuditReviewWindowDaysRangeRule(1000, 1000),
        ),
    )
}
