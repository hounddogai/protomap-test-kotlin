package com.acme.transport.customs.broker.lenientbrokerestimate

/** A rule a lenient broker estimate must pass. Check returns its problems, or none. */
interface LenientBrokerEstimateRule {
    fun check(item: LenientBrokerEstimate): MutableList<String>
}

data class LenientBrokerEstimateTitleLengthRule(var maxLength: Int) : LenientBrokerEstimateRule {
    override fun check(item: LenientBrokerEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientBrokerEstimateCodePrefixRule(var prefix: String) : LenientBrokerEstimateRule {
    override fun check(item: LenientBrokerEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientBrokerEstimateRequiredTitleRule(var message: String) : LenientBrokerEstimateRule {
    override fun check(item: LenientBrokerEstimate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientBrokerEstimateReviewerLengthRule(var maxLength: Int) : LenientBrokerEstimateRule {
    override fun check(item: LenientBrokerEstimate): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientBrokerEstimateCodeLengthRule(var maxLength: Int) : LenientBrokerEstimateRule {
    override fun check(item: LenientBrokerEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientBrokerEstimateRequiredReviewerRule(var message: String) : LenientBrokerEstimateRule {
    override fun check(item: LenientBrokerEstimate): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient broker estimate. */
data class LenientBrokerEstimateValidator(var rules: MutableList<LenientBrokerEstimateRule>) {
    fun validate(item: LenientBrokerEstimate): MutableList<String> {
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

    fun requireValid(item: LenientBrokerEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientBrokerEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientBrokerEstimateValidator(): LenientBrokerEstimateValidator {
    return LenientBrokerEstimateValidator(
        mutableListOf<LenientBrokerEstimateRule>(
            LenientBrokerEstimateTitleLengthRule(80),
            LenientBrokerEstimateCodePrefixRule("X"),
            LenientBrokerEstimateRequiredTitleRule("lenient broker estimate needs this field"),
            LenientBrokerEstimateReviewerLengthRule(255),
            LenientBrokerEstimateCodeLengthRule(32),
            LenientBrokerEstimateRequiredReviewerRule("lenient broker estimate needs this field"),
        ),
    )
}
