package com.acme.transport.network.node.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a lenient node manifest must pass. Check returns its problems, or none. */
interface LenientNodeManifestRule {
    fun check(item: LenientNodeManifest): MutableList<String>
}

data class LenientNodeManifestArchivedGuardRule(var limit: Double) : LenientNodeManifestRule {
    override fun check(item: LenientNodeManifest): MutableList<String> {
        if (item.archived && item.rate > limit) {
            return mutableListOf<String>("rate ${item.rate} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class LenientNodeManifestAllowedCurrencyRule(var allowed: MutableList<String>) : LenientNodeManifestRule {
    override fun check(item: LenientNodeManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.currency))) {
            return mutableListOf<String>("currency ${item.currency} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class LenientNodeManifestCurrencyLengthRule(var maxLength: Int) : LenientNodeManifestRule {
    override fun check(item: LenientNodeManifest): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient node manifest. */
data class LenientNodeManifestValidator(var rules: MutableList<LenientNodeManifestRule>) {
    fun validate(item: LenientNodeManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientNodeManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientNodeManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientNodeManifestValidator(): LenientNodeManifestValidator {
    return LenientNodeManifestValidator(
        mutableListOf<LenientNodeManifestRule>(
            LenientNodeManifestArchivedGuardRule(1.4),
            LenientNodeManifestAllowedCurrencyRule(mutableListOf<String>()),
            LenientNodeManifestCurrencyLengthRule(3600),
        ),
    )
}
