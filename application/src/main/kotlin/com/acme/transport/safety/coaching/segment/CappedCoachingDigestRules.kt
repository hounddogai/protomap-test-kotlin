package com.acme.transport.safety.coaching.segment

import com.acme.transport.kit.text.normalize

/** A rule a capped coaching digest must pass. Check returns its problems, or none. */
interface CappedCoachingDigestRule {
    fun check(item: CappedCoachingDigest): MutableList<String>
}

data class CappedCoachingDigestRequiredNameRule(var message: String) : CappedCoachingDigestRule {
    override fun check(item: CappedCoachingDigest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCoachingDigestAllowedSummaryRule(var allowed: MutableList<String>) : CappedCoachingDigestRule {
    override fun check(item: CappedCoachingDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class CappedCoachingDigestRequiredPhoneRule(var message: String) : CappedCoachingDigestRule {
    override fun check(item: CappedCoachingDigest): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCoachingDigestRequiredSummaryRule(var message: String) : CappedCoachingDigestRule {
    override fun check(item: CappedCoachingDigest): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCoachingDigestSummaryLengthRule(var maxLength: Int) : CappedCoachingDigestRule {
    override fun check(item: CappedCoachingDigest): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedCoachingDigestNameLengthRule(var maxLength: Int) : CappedCoachingDigestRule {
    override fun check(item: CappedCoachingDigest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped coaching digest. */
data class CappedCoachingDigestValidator(var rules: MutableList<CappedCoachingDigestRule>) {
    fun validate(item: CappedCoachingDigest): MutableList<String> {
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

    fun requireValid(item: CappedCoachingDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCoachingDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCoachingDigestValidator(): CappedCoachingDigestValidator {
    return CappedCoachingDigestValidator(
        mutableListOf<CappedCoachingDigestRule>(
            CappedCoachingDigestRequiredNameRule("capped coaching digest needs this field"),
            CappedCoachingDigestAllowedSummaryRule(mutableListOf<String>()),
            CappedCoachingDigestRequiredPhoneRule("capped coaching digest needs a value"),
            CappedCoachingDigestRequiredSummaryRule("capped coaching digest needs a value"),
            CappedCoachingDigestSummaryLengthRule(64),
            CappedCoachingDigestNameLengthRule(80),
        ),
    )
}
