package com.acme.transport.lastmile.parcel.profile

import com.acme.transport.kit.text.normalize

/** A rule a capped parcel digest must pass. Check returns its problems, or none. */
interface CappedParcelDigestRule {
    fun check(item: CappedParcelDigest): MutableList<String>
}

data class CappedParcelDigestRequiredTitleRule(var message: String) : CappedParcelDigestRule {
    override fun check(item: CappedParcelDigest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedParcelDigestRequiredCodeRule(var message: String) : CappedParcelDigestRule {
    override fun check(item: CappedParcelDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedParcelDigestAllowedTitleRule(var allowed: MutableList<String>) : CappedParcelDigestRule {
    override fun check(item: CappedParcelDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped parcel digest. */
data class CappedParcelDigestValidator(var rules: MutableList<CappedParcelDigestRule>) {
    fun validate(item: CappedParcelDigest): MutableList<String> {
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

    fun requireValid(item: CappedParcelDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedParcelDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedParcelDigestValidator(): CappedParcelDigestValidator {
    return CappedParcelDigestValidator(
        mutableListOf<CappedParcelDigestRule>(
            CappedParcelDigestRequiredTitleRule("capped parcel digest needs a value"),
            CappedParcelDigestRequiredCodeRule("capped parcel digest needs a value"),
            CappedParcelDigestAllowedTitleRule(mutableListOf<String>("fallback")),
        ),
    )
}
