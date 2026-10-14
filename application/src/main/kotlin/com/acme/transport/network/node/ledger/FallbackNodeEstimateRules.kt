package com.acme.transport.network.node.ledger

/** A rule a fallback node estimate must pass. Check returns its problems, or none. */
interface FallbackNodeEstimateRule {
    fun check(item: FallbackNodeEstimate): MutableList<String>
}

data class FallbackNodeEstimateExpiresAtRangeRule(var min: Int, var max: Int) : FallbackNodeEstimateRule {
    override fun check(item: FallbackNodeEstimate): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackNodeEstimateContactEmailLengthRule(var maxLength: Int) : FallbackNodeEstimateRule {
    override fun check(item: FallbackNodeEstimate): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackNodeEstimateCodeLengthRule(var maxLength: Int) : FallbackNodeEstimateRule {
    override fun check(item: FallbackNodeEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback node estimate. */
data class FallbackNodeEstimateValidator(var rules: MutableList<FallbackNodeEstimateRule>) {
    fun validate(item: FallbackNodeEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackNodeEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackNodeEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackNodeEstimateValidator(): FallbackNodeEstimateValidator {
    return FallbackNodeEstimateValidator(
        mutableListOf<FallbackNodeEstimateRule>(
            FallbackNodeEstimateExpiresAtRangeRule(255, 86400),
            FallbackNodeEstimateContactEmailLengthRule(3600),
            FallbackNodeEstimateCodeLengthRule(32),
        ),
    )
}
