package com.acme.transport.network.cutoff.tier

import com.acme.transport.kit.text.normalize

/** A rule a strict cutoff rule must pass. Check returns its problems, or none. */
interface StrictCutoffRuleRule {
    fun check(item: StrictCutoffRule): MutableList<String>
}

data class StrictCutoffRuleCodeLengthRule(var maxLength: Int) : StrictCutoffRuleRule {
    override fun check(item: StrictCutoffRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictCutoffRuleLabelLengthRule(var maxLength: Int) : StrictCutoffRuleRule {
    override fun check(item: StrictCutoffRule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictCutoffRuleCodePrefixRule(var prefix: String) : StrictCutoffRuleRule {
    override fun check(item: StrictCutoffRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictCutoffRuleAllowedLabelRule(var allowed: MutableList<String>) : StrictCutoffRuleRule {
    override fun check(item: StrictCutoffRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict cutoff rule. */
data class StrictCutoffRuleValidator(var rules: MutableList<StrictCutoffRuleRule>) {
    fun validate(item: StrictCutoffRule): MutableList<String> {
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

    fun requireValid(item: StrictCutoffRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictCutoffRule>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictCutoffRuleValidator(): StrictCutoffRuleValidator {
    return StrictCutoffRuleValidator(
        mutableListOf<StrictCutoffRuleRule>(
            StrictCutoffRuleCodeLengthRule(32),
            StrictCutoffRuleLabelLengthRule(32),
            StrictCutoffRuleCodePrefixRule("REF"),
            StrictCutoffRuleAllowedLabelRule(mutableListOf<String>("adaptive")),
        ),
    )
}
