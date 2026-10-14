package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.kit.text.normalize

/** A rule a legacy backhaul baseline must pass. Check returns its problems, or none. */
interface LegacyBackhaulBaselineRule {
    fun check(item: LegacyBackhaulBaseline): MutableList<String>
}

data class LegacyBackhaulBaselineAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : LegacyBackhaulBaselineRule {
    override fun check(item: LegacyBackhaulBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class LegacyBackhaulBaselineRequiredCodeRule(var message: String) : LegacyBackhaulBaselineRule {
    override fun check(item: LegacyBackhaulBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyBackhaulBaselineRequiredNameRule(var message: String) : LegacyBackhaulBaselineRule {
    override fun check(item: LegacyBackhaulBaseline): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy backhaul baseline. */
data class LegacyBackhaulBaselineValidator(var rules: MutableList<LegacyBackhaulBaselineRule>) {
    fun validate(item: LegacyBackhaulBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyBackhaulBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyBackhaulBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyBackhaulBaselineValidator(): LegacyBackhaulBaselineValidator {
    return LegacyBackhaulBaselineValidator(
        mutableListOf<LegacyBackhaulBaselineRule>(
            LegacyBackhaulBaselineAllowedExternalRefRule(
                mutableListOf<String>("fallback", "fallback", "manual"),
            ),
            LegacyBackhaulBaselineRequiredCodeRule("legacy backhaul baseline needs an entry"),
            LegacyBackhaulBaselineRequiredNameRule("legacy backhaul baseline needs an entry"),
        ),
    )
}
