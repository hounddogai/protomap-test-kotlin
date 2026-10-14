package com.acme.transport.emissions.routeoffset.offsetrule

import com.acme.transport.kit.text.normalize

/** A rule a strict route offset rule must pass. Check returns its problems, or none. */
interface StrictRouteOffsetRuleRule {
    fun check(item: StrictRouteOffsetRule): MutableList<String>
}

data class StrictRouteOffsetRuleCodeLengthRule(var maxLength: Int) : StrictRouteOffsetRuleRule {
    override fun check(item: StrictRouteOffsetRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRouteOffsetRuleAllowedCodeRule(var allowed: MutableList<String>) : StrictRouteOffsetRuleRule {
    override fun check(item: StrictRouteOffsetRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class StrictRouteOffsetRuleRequiredLabelRule(var message: String) : StrictRouteOffsetRuleRule {
    override fun check(item: StrictRouteOffsetRule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict route offset rule. */
data class StrictRouteOffsetRuleValidator(var rules: MutableList<StrictRouteOffsetRuleRule>) {
    fun validate(item: StrictRouteOffsetRule): MutableList<String> {
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

    fun requireValid(item: StrictRouteOffsetRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRouteOffsetRule>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRouteOffsetRuleValidator(): StrictRouteOffsetRuleValidator {
    return StrictRouteOffsetRuleValidator(
        mutableListOf<StrictRouteOffsetRuleRule>(
            StrictRouteOffsetRuleCodeLengthRule(1000),
            StrictRouteOffsetRuleAllowedCodeRule(mutableListOf<String>("manual", "tiered")),
            StrictRouteOffsetRuleRequiredLabelRule("strict route offset rule needs an entry"),
        ),
    )
}
