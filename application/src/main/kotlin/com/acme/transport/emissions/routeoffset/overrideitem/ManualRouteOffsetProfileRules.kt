package com.acme.transport.emissions.routeoffset.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a manual route offset profile must pass. Check returns its problems, or none. */
interface ManualRouteOffsetProfileRule {
    fun check(item: ManualRouteOffsetProfile): MutableList<String>
}

data class ManualRouteOffsetProfileRequiredCurrencyRule(var message: String) : ManualRouteOffsetProfileRule {
    override fun check(item: ManualRouteOffsetProfile): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualRouteOffsetProfileAllowedCodeRule(
    var allowed: MutableList<String>,
) : ManualRouteOffsetProfileRule {
    override fun check(item: ManualRouteOffsetProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class ManualRouteOffsetProfileQuantityRangeRule(
    var min: Int,
    var max: Int,
) : ManualRouteOffsetProfileRule {
    override fun check(item: ManualRouteOffsetProfile): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class ManualRouteOffsetProfileCodePrefixRule(var prefix: String) : ManualRouteOffsetProfileRule {
    override fun check(item: ManualRouteOffsetProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual route offset profile. */
data class ManualRouteOffsetProfileValidator(var rules: MutableList<ManualRouteOffsetProfileRule>) {
    fun validate(item: ManualRouteOffsetProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualRouteOffsetProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualRouteOffsetProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultManualRouteOffsetProfileValidator(): ManualRouteOffsetProfileValidator {
    return ManualRouteOffsetProfileValidator(
        mutableListOf<ManualRouteOffsetProfileRule>(
            ManualRouteOffsetProfileRequiredCurrencyRule("manual route offset profile needs this field"),
            ManualRouteOffsetProfileAllowedCodeRule(mutableListOf<String>()),
            ManualRouteOffsetProfileQuantityRangeRule(16, 16),
            ManualRouteOffsetProfileCodePrefixRule("X"),
        ),
    )
}
