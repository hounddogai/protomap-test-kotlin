package com.acme.transport.freightpay.audit.legacyauditpolicy

import com.acme.transport.kit.text.normalize

/** A rule a capped audit digest must pass. Check returns its problems, or none. */
interface CappedAuditDigestRule {
    fun check(item: CappedAuditDigest): MutableList<String>
}

data class CappedAuditDigestAllowedNameRule(var allowed: MutableList<String>) : CappedAuditDigestRule {
    override fun check(item: CappedAuditDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class CappedAuditDigestMinimumRangeRule(var min: Double, var max: Double) : CappedAuditDigestRule {
    override fun check(item: CappedAuditDigest): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedAuditDigestRequiredReviewerRule(var message: String) : CappedAuditDigestRule {
    override fun check(item: CappedAuditDigest): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedAuditDigestScoreRangeRule(var min: Double, var max: Double) : CappedAuditDigestRule {
    override fun check(item: CappedAuditDigest): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped audit digest. */
data class CappedAuditDigestValidator(var rules: MutableList<CappedAuditDigestRule>) {
    fun validate(item: CappedAuditDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedAuditDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedAuditDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedAuditDigestValidator(): CappedAuditDigestValidator {
    return CappedAuditDigestValidator(
        mutableListOf<CappedAuditDigestRule>(
            CappedAuditDigestAllowedNameRule(mutableListOf<String>("weighted")),
            CappedAuditDigestMinimumRangeRule(0.7, 2656.6),
            CappedAuditDigestRequiredReviewerRule("capped audit digest needs a value"),
            CappedAuditDigestScoreRangeRule(4.3, 58.8),
        ),
    )
}
