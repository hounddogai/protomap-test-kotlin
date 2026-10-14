package com.acme.transport.customs.broker.request

import com.acme.transport.kit.text.normalize

/** A rule a fallback broker bundle must pass. Check returns its problems, or none. */
interface FallbackBrokerBundleRule {
    fun check(item: FallbackBrokerBundle): MutableList<String>
}

data class FallbackBrokerBundleAllowedCodeRule(var allowed: MutableList<String>) : FallbackBrokerBundleRule {
    override fun check(item: FallbackBrokerBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class FallbackBrokerBundleCodeLengthRule(var maxLength: Int) : FallbackBrokerBundleRule {
    override fun check(item: FallbackBrokerBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackBrokerBundleVisibleGuardRule(var limit: Double) : FallbackBrokerBundleRule {
    override fun check(item: FallbackBrokerBundle): MutableList<String> {
        if (item.visible && item.minimum > limit) {
            return mutableListOf<String>("minimum ${item.minimum} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback broker bundle. */
data class FallbackBrokerBundleValidator(var rules: MutableList<FallbackBrokerBundleRule>) {
    fun validate(item: FallbackBrokerBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackBrokerBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackBrokerBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackBrokerBundleValidator(): FallbackBrokerBundleValidator {
    return FallbackBrokerBundleValidator(
        mutableListOf<FallbackBrokerBundleRule>(
            FallbackBrokerBundleAllowedCodeRule(mutableListOf<String>("adaptive")),
            FallbackBrokerBundleCodeLengthRule(80),
            FallbackBrokerBundleVisibleGuardRule(4.0),
        ),
    )
}
