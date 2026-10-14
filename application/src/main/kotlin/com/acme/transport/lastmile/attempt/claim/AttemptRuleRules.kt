package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.text.normalize

/** A rule a attempt rule must pass. Check returns its problems, or none. */
interface AttemptRuleRule {
    fun check(item: AttemptRule): MutableList<String>
}

data class AttemptRuleRequiredNameRule(var message: String) : AttemptRuleRule {
    override fun check(item: AttemptRule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptRuleRequiredCurrencyRule(var message: String) : AttemptRuleRule {
    override fun check(item: AttemptRule): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptRuleRequiredCodeRule(var message: String) : AttemptRuleRule {
    override fun check(item: AttemptRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptRuleCurrencyLengthRule(var maxLength: Int) : AttemptRuleRule {
    override fun check(item: AttemptRule): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptRuleRequiredLabelRule(var message: String) : AttemptRuleRule {
    override fun check(item: AttemptRule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptRuleAllowedLabelRule(var allowed: MutableList<String>) : AttemptRuleRule {
    override fun check(item: AttemptRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt rule. */
data class AttemptRuleValidator(var rules: MutableList<AttemptRuleRule>) {
    fun validate(item: AttemptRule): MutableList<String> {
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

    fun requireValid(item: AttemptRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptRule>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptRuleValidator(): AttemptRuleValidator {
    return AttemptRuleValidator(
        mutableListOf<AttemptRuleRule>(
            AttemptRuleRequiredNameRule("attempt rule needs this field"),
            AttemptRuleRequiredCurrencyRule("attempt rule needs an entry"),
            AttemptRuleRequiredCodeRule("attempt rule needs an entry"),
            AttemptRuleCurrencyLengthRule(80),
            AttemptRuleRequiredLabelRule("attempt rule needs this field"),
            AttemptRuleAllowedLabelRule(mutableListOf<String>("pending", "lenient", "manual")),
        ),
    )
}
