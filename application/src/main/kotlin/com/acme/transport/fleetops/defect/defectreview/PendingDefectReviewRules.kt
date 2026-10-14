package com.acme.transport.fleetops.defect.defectreview

/** A rule a pending defect review must pass. Check returns its problems, or none. */
interface PendingDefectReviewRule {
    fun check(item: PendingDefectReview): MutableList<String>
}

data class PendingDefectReviewRequiredCodeRule(var message: String) : PendingDefectReviewRule {
    override fun check(item: PendingDefectReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDefectReviewRequiredCurrencyRule(var message: String) : PendingDefectReviewRule {
    override fun check(item: PendingDefectReview): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDefectReviewCodePrefixRule(var prefix: String) : PendingDefectReviewRule {
    override fun check(item: PendingDefectReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingDefectReviewRequiredCategoryRule(var message: String) : PendingDefectReviewRule {
    override fun check(item: PendingDefectReview): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending defect review. */
data class PendingDefectReviewValidator(var rules: MutableList<PendingDefectReviewRule>) {
    fun validate(item: PendingDefectReview): MutableList<String> {
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

    fun requireValid(item: PendingDefectReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDefectReview>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDefectReviewValidator(): PendingDefectReviewValidator {
    return PendingDefectReviewValidator(
        mutableListOf<PendingDefectReviewRule>(
            PendingDefectReviewRequiredCodeRule("pending defect review needs this field"),
            PendingDefectReviewRequiredCurrencyRule("pending defect review needs a value"),
            PendingDefectReviewCodePrefixRule("ACM"),
            PendingDefectReviewRequiredCategoryRule("pending defect review needs this field"),
        ),
    )
}
