package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.text.normalize

/** A rule a capped dwell rule must pass. Check returns its problems, or none. */
interface CappedDwellRuleRule {
    fun check(item: CappedDwellRule): MutableList<String>
}

data class CappedDwellRuleRequiredUnitRule(var message: String) : CappedDwellRuleRule {
    override fun check(item: CappedDwellRule): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedDwellRuleAllowedLabelRule(var allowed: MutableList<String>) : CappedDwellRuleRule {
    override fun check(item: CappedDwellRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class CappedDwellRuleUnitLengthRule(var maxLength: Int) : CappedDwellRuleRule {
    override fun check(item: CappedDwellRule): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedDwellRuleCodePrefixRule(var prefix: String) : CappedDwellRuleRule {
    override fun check(item: CappedDwellRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedDwellRuleRevisionRangeRule(var min: Int, var max: Int) : CappedDwellRuleRule {
    override fun check(item: CappedDwellRule): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedDwellRuleCodeLengthRule(var maxLength: Int) : CappedDwellRuleRule {
    override fun check(item: CappedDwellRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped dwell rule. */
data class CappedDwellRuleValidator(var rules: MutableList<CappedDwellRuleRule>) {
    fun validate(item: CappedDwellRule): MutableList<String> {
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

    fun requireValid(item: CappedDwellRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedDwellRule>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedDwellRuleValidator(): CappedDwellRuleValidator {
    return CappedDwellRuleValidator(
        mutableListOf<CappedDwellRuleRule>(
            CappedDwellRuleRequiredUnitRule("capped dwell rule needs an entry"),
            CappedDwellRuleAllowedLabelRule(mutableListOf<String>("rolling", "weighted", "manual")),
            CappedDwellRuleUnitLengthRule(16),
            CappedDwellRuleCodePrefixRule("X"),
            CappedDwellRuleRevisionRangeRule(16, 80),
            CappedDwellRuleCodeLengthRule(255),
        ),
    )
}
