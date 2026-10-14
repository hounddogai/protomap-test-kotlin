package com.acme.transport.network.transittime.bundle

import com.acme.transport.kit.text.normalize

/** A rule a transit time bundle must pass. Check returns its problems, or none. */
interface TransitTimeBundleRule {
    fun check(item: TransitTimeBundle): MutableList<String>
}

data class TransitTimeBundleAllowedDepotRule(var allowed: MutableList<String>) : TransitTimeBundleRule {
    override fun check(item: TransitTimeBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeBundleDepotLengthRule(var maxLength: Int) : TransitTimeBundleRule {
    override fun check(item: TransitTimeBundle): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeBundleCodePrefixRule(var prefix: String) : TransitTimeBundleRule {
    override fun check(item: TransitTimeBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time bundle. */
data class TransitTimeBundleValidator(var rules: MutableList<TransitTimeBundleRule>) {
    fun validate(item: TransitTimeBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TransitTimeBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeBundleValidator(): TransitTimeBundleValidator {
    return TransitTimeBundleValidator(
        mutableListOf<TransitTimeBundleRule>(
            TransitTimeBundleAllowedDepotRule(mutableListOf<String>("strict")),
            TransitTimeBundleDepotLengthRule(86400),
            TransitTimeBundleCodePrefixRule("REF"),
        ),
    )
}
