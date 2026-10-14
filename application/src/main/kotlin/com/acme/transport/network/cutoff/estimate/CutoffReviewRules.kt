package com.acme.transport.network.cutoff.estimate

import com.acme.transport.kit.text.normalize

/** A rule a cutoff review must pass. Check returns its problems, or none. */
interface CutoffReviewRule {
    fun check(item: CutoffReview): MutableList<String>
}

data class CutoffReviewAllowedCodeRule(var allowed: MutableList<String>) : CutoffReviewRule {
    override fun check(item: CutoffReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class CutoffReviewCapacityRangeRule(var min: Int, var max: Int) : CutoffReviewRule {
    override fun check(item: CutoffReview): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class CutoffReviewCodePrefixRule(var prefix: String) : CutoffReviewRule {
    override fun check(item: CutoffReview): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CutoffReviewAttemptsRangeRule(var min: Int, var max: Int) : CutoffReviewRule {
    override fun check(item: CutoffReview): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff review. */
data class CutoffReviewValidator(var rules: MutableList<CutoffReviewRule>) {
    fun validate(item: CutoffReview): MutableList<String> {
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

    fun requireValid(item: CutoffReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffReview>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffReviewValidator(): CutoffReviewValidator {
    return CutoffReviewValidator(
        mutableListOf<CutoffReviewRule>(
            CutoffReviewAllowedCodeRule(mutableListOf<String>()),
            CutoffReviewCapacityRangeRule(16, 255),
            CutoffReviewCodePrefixRule("REF"),
            CutoffReviewAttemptsRangeRule(64, 1000),
        ),
    )
}
