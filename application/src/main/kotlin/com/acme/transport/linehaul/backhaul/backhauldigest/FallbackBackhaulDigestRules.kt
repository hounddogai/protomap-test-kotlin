package com.acme.transport.linehaul.backhaul.backhauldigest

import com.acme.transport.kit.text.normalize

/** A rule a fallback backhaul digest must pass. Check returns its problems, or none. */
interface FallbackBackhaulDigestRule {
    fun check(item: FallbackBackhaulDigest): MutableList<String>
}

data class FallbackBackhaulDigestAllowedCodeRule(
    var allowed: MutableList<String>,
) : FallbackBackhaulDigestRule {
    override fun check(item: FallbackBackhaulDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulDigestStopCountRangeRule(var min: Int, var max: Int) : FallbackBackhaulDigestRule {
    override fun check(item: FallbackBackhaulDigest): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class FallbackBackhaulDigestRequiredCodeRule(var message: String) : FallbackBackhaulDigestRule {
    override fun check(item: FallbackBackhaulDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulDigestApprovedGuardRule(var limit: Int) : FallbackBackhaulDigestRule {
    override fun check(item: FallbackBackhaulDigest): MutableList<String> {
        if (item.approved && item.stopCount > limit) {
            return mutableListOf<String>("stop count ${item.stopCount} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulDigestRequiredRegionRule(var message: String) : FallbackBackhaulDigestRule {
    override fun check(item: FallbackBackhaulDigest): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulDigestCodePrefixRule(var prefix: String) : FallbackBackhaulDigestRule {
    override fun check(item: FallbackBackhaulDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback backhaul digest. */
data class FallbackBackhaulDigestValidator(var rules: MutableList<FallbackBackhaulDigestRule>) {
    fun validate(item: FallbackBackhaulDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackBackhaulDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackBackhaulDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackBackhaulDigestValidator(): FallbackBackhaulDigestValidator {
    return FallbackBackhaulDigestValidator(
        mutableListOf<FallbackBackhaulDigestRule>(
            FallbackBackhaulDigestAllowedCodeRule(mutableListOf<String>("global", "rolling")),
            FallbackBackhaulDigestStopCountRangeRule(16, 1000),
            FallbackBackhaulDigestRequiredCodeRule("fallback backhaul digest needs this field"),
            FallbackBackhaulDigestApprovedGuardRule(86400),
            FallbackBackhaulDigestRequiredRegionRule("fallback backhaul digest needs a value"),
            FallbackBackhaulDigestCodePrefixRule("REF"),
        ),
    )
}
