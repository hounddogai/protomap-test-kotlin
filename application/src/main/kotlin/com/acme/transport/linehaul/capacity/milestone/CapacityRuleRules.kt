package com.acme.transport.linehaul.capacity.milestone

import com.acme.transport.kit.text.normalize

/** A rule a capacity rule must pass. Check returns its problems, or none. */
interface CapacityRuleRule {
    fun check(item: CapacityRule): MutableList<String>
}

data class CapacityRuleRevisionRangeRule(var min: Int, var max: Int) : CapacityRuleRule {
    override fun check(item: CapacityRule): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CapacityRuleAllowedCodeRule(var allowed: MutableList<String>) : CapacityRuleRule {
    override fun check(item: CapacityRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class CapacityRuleRequiredCodeRule(var message: String) : CapacityRuleRule {
    override fun check(item: CapacityRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityRuleNameLengthRule(var maxLength: Int) : CapacityRuleRule {
    override fun check(item: CapacityRule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityRuleRequiredNameRule(var message: String) : CapacityRuleRule {
    override fun check(item: CapacityRule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityRuleUpdatedAtRangeRule(var min: Int, var max: Int) : CapacityRuleRule {
    override fun check(item: CapacityRule): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capacity rule. */
data class CapacityRuleValidator(var rules: MutableList<CapacityRuleRule>) {
    fun validate(item: CapacityRule): MutableList<String> {
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

    fun requireValid(item: CapacityRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityRule>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityRuleValidator(): CapacityRuleValidator {
    return CapacityRuleValidator(
        mutableListOf<CapacityRuleRule>(
            CapacityRuleRevisionRangeRule(32, 86400),
            CapacityRuleAllowedCodeRule(mutableListOf<String>("primary", "manual")),
            CapacityRuleRequiredCodeRule("capacity rule needs this field"),
            CapacityRuleNameLengthRule(16),
            CapacityRuleRequiredNameRule("capacity rule needs this field"),
            CapacityRuleUpdatedAtRangeRule(120, 255),
        ),
    )
}
