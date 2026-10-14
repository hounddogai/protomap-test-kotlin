package com.acme.transport.lastmile.redelivery.threshold

/** A rule a redelivery estimate must pass. Check returns its problems, or none. */
interface RedeliveryEstimateRule {
    fun check(item: RedeliveryEstimate): MutableList<String>
}

data class RedeliveryEstimateVisibleGuardRule(var limit: Int) : RedeliveryEstimateRule {
    override fun check(item: RedeliveryEstimate): MutableList<String> {
        if (item.visible && item.sequence > limit) {
            return mutableListOf<String>("sequence ${item.sequence} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryEstimateRequiredNameRule(var message: String) : RedeliveryEstimateRule {
    override fun check(item: RedeliveryEstimate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryEstimateRequiredCodeRule(var message: String) : RedeliveryEstimateRule {
    override fun check(item: RedeliveryEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryEstimateNameLengthRule(var maxLength: Int) : RedeliveryEstimateRule {
    override fun check(item: RedeliveryEstimate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryEstimateCodeLengthRule(var maxLength: Int) : RedeliveryEstimateRule {
    override fun check(item: RedeliveryEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryEstimateReviewerLengthRule(var maxLength: Int) : RedeliveryEstimateRule {
    override fun check(item: RedeliveryEstimate): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery estimate. */
data class RedeliveryEstimateValidator(var rules: MutableList<RedeliveryEstimateRule>) {
    fun validate(item: RedeliveryEstimate): MutableList<String> {
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

    fun requireValid(item: RedeliveryEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryEstimateValidator(): RedeliveryEstimateValidator {
    return RedeliveryEstimateValidator(
        mutableListOf<RedeliveryEstimateRule>(
            RedeliveryEstimateVisibleGuardRule(3600),
            RedeliveryEstimateRequiredNameRule("redelivery estimate needs an entry"),
            RedeliveryEstimateRequiredCodeRule("redelivery estimate needs a value"),
            RedeliveryEstimateNameLengthRule(120),
            RedeliveryEstimateCodeLengthRule(80),
            RedeliveryEstimateReviewerLengthRule(3600),
        ),
    )
}
