package com.acme.transport.terminal.dwell.slot

import com.acme.transport.kit.text.normalize

/** A rule a fallback dwell rule must pass. Check returns its problems, or none. */
interface FallbackDwellRuleRule {
    fun check(item: FallbackDwellRule): MutableList<String>
}

data class FallbackDwellRuleSummaryLengthRule(var maxLength: Int) : FallbackDwellRuleRule {
    override fun check(item: FallbackDwellRule): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellRuleRequiredSummaryRule(var message: String) : FallbackDwellRuleRule {
    override fun check(item: FallbackDwellRule): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellRuleAllowedCodeRule(var allowed: MutableList<String>) : FallbackDwellRuleRule {
    override fun check(item: FallbackDwellRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellRuleRequiredCodeRule(var message: String) : FallbackDwellRuleRule {
    override fun check(item: FallbackDwellRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellRuleRequiredTitleRule(var message: String) : FallbackDwellRuleRule {
    override fun check(item: FallbackDwellRule): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback dwell rule. */
data class FallbackDwellRuleValidator(var rules: MutableList<FallbackDwellRuleRule>) {
    fun validate(item: FallbackDwellRule): MutableList<String> {
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

    fun requireValid(item: FallbackDwellRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDwellRule>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDwellRuleValidator(): FallbackDwellRuleValidator {
    return FallbackDwellRuleValidator(
        mutableListOf<FallbackDwellRuleRule>(
            FallbackDwellRuleSummaryLengthRule(120),
            FallbackDwellRuleRequiredSummaryRule("fallback dwell rule needs this field"),
            FallbackDwellRuleAllowedCodeRule(mutableListOf<String>("manual")),
            FallbackDwellRuleRequiredCodeRule("fallback dwell rule needs this field"),
            FallbackDwellRuleRequiredTitleRule("fallback dwell rule needs a value"),
        ),
    )
}
