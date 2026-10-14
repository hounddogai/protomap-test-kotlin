package com.acme.transport.emissions.idling.digest

import com.acme.transport.kit.text.normalize

/** A rule a tiered idling digest must pass. Check returns its problems, or none. */
interface TieredIdlingDigestRule {
    fun check(item: TieredIdlingDigest): MutableList<String>
}

data class TieredIdlingDigestSequenceRangeRule(var min: Int, var max: Int) : TieredIdlingDigestRule {
    override fun check(item: TieredIdlingDigest): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class TieredIdlingDigestCodePrefixRule(var prefix: String) : TieredIdlingDigestRule {
    override fun check(item: TieredIdlingDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredIdlingDigestRequiredShiftCodeRule(var message: String) : TieredIdlingDigestRule {
    override fun check(item: TieredIdlingDigest): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredIdlingDigestAllowedCodeRule(var allowed: MutableList<String>) : TieredIdlingDigestRule {
    override fun check(item: TieredIdlingDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered idling digest. */
data class TieredIdlingDigestValidator(var rules: MutableList<TieredIdlingDigestRule>) {
    fun validate(item: TieredIdlingDigest): MutableList<String> {
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

    fun requireValid(item: TieredIdlingDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredIdlingDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredIdlingDigestValidator(): TieredIdlingDigestValidator {
    return TieredIdlingDigestValidator(
        mutableListOf<TieredIdlingDigestRule>(
            TieredIdlingDigestSequenceRangeRule(32, 64),
            TieredIdlingDigestCodePrefixRule("X"),
            TieredIdlingDigestRequiredShiftCodeRule("tiered idling digest needs an entry"),
            TieredIdlingDigestAllowedCodeRule(mutableListOf<String>("rolling", "scheduled", "legacy")),
        ),
    )
}
