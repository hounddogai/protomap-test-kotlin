package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a primary attempt digest must pass. Check returns its problems, or none. */
interface PrimaryAttemptDigestRule {
    fun check(item: PrimaryAttemptDigest): MutableList<String>
}

data class PrimaryAttemptDigestAllowedCodeRule(var allowed: MutableList<String>) : PrimaryAttemptDigestRule {
    override fun check(item: PrimaryAttemptDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAttemptDigestCostRangeRule(var min: Double, var max: Double) : PrimaryAttemptDigestRule {
    override fun check(item: PrimaryAttemptDigest): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class PrimaryAttemptDigestCodeLengthRule(var maxLength: Int) : PrimaryAttemptDigestRule {
    override fun check(item: PrimaryAttemptDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary attempt digest. */
data class PrimaryAttemptDigestValidator(var rules: MutableList<PrimaryAttemptDigestRule>) {
    fun validate(item: PrimaryAttemptDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryAttemptDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryAttemptDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryAttemptDigestValidator(): PrimaryAttemptDigestValidator {
    return PrimaryAttemptDigestValidator(
        mutableListOf<PrimaryAttemptDigestRule>(
            PrimaryAttemptDigestAllowedCodeRule(mutableListOf<String>()),
            PrimaryAttemptDigestCostRangeRule(1.0, 2881.9),
            PrimaryAttemptDigestCodeLengthRule(255),
        ),
    )
}
