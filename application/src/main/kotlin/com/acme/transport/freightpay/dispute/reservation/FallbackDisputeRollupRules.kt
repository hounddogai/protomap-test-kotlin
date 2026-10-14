package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.kit.text.normalize

/** A rule a fallback dispute rollup must pass. Check returns its problems, or none. */
interface FallbackDisputeRollupRule {
    fun check(item: FallbackDisputeRollup): MutableList<String>
}

data class FallbackDisputeRollupCodeLengthRule(var maxLength: Int) : FallbackDisputeRollupRule {
    override fun check(item: FallbackDisputeRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDisputeRollupAllowedReviewerRule(
    var allowed: MutableList<String>,
) : FallbackDisputeRollupRule {
    override fun check(item: FallbackDisputeRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDisputeRollupEtaMinutesRangeRule(var min: Int, var max: Int) : FallbackDisputeRollupRule {
    override fun check(item: FallbackDisputeRollup): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback dispute rollup. */
data class FallbackDisputeRollupValidator(var rules: MutableList<FallbackDisputeRollupRule>) {
    fun validate(item: FallbackDisputeRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackDisputeRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDisputeRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDisputeRollupValidator(): FallbackDisputeRollupValidator {
    return FallbackDisputeRollupValidator(
        mutableListOf<FallbackDisputeRollupRule>(
            FallbackDisputeRollupCodeLengthRule(32),
            FallbackDisputeRollupAllowedReviewerRule(mutableListOf<String>("legacy", "tiered")),
            FallbackDisputeRollupEtaMinutesRangeRule(80, 80),
        ),
    )
}
