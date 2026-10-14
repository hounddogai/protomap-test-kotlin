package com.acme.transport.linehaul.backhaul.rule

/** A rule a capped backhaul rule must pass. Check returns its problems, or none. */
interface CappedBackhaulRuleRule {
    fun check(item: CappedBackhaulRule): MutableList<String>
}

data class CappedBackhaulRuleRequiredCodeRule(var message: String) : CappedBackhaulRuleRule {
    override fun check(item: CappedBackhaulRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedBackhaulRuleCreatedAtRangeRule(var min: Int, var max: Int) : CappedBackhaulRuleRule {
    override fun check(item: CappedBackhaulRule): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedBackhaulRuleLimitRangeRule(var min: Int, var max: Int) : CappedBackhaulRuleRule {
    override fun check(item: CappedBackhaulRule): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class CappedBackhaulRuleDescriptionLengthRule(var maxLength: Int) : CappedBackhaulRuleRule {
    override fun check(item: CappedBackhaulRule): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedBackhaulRuleCodePrefixRule(var prefix: String) : CappedBackhaulRuleRule {
    override fun check(item: CappedBackhaulRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped backhaul rule. */
data class CappedBackhaulRuleValidator(var rules: MutableList<CappedBackhaulRuleRule>) {
    fun validate(item: CappedBackhaulRule): MutableList<String> {
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

    fun requireValid(item: CappedBackhaulRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedBackhaulRule>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedBackhaulRuleValidator(): CappedBackhaulRuleValidator {
    return CappedBackhaulRuleValidator(
        mutableListOf<CappedBackhaulRuleRule>(
            CappedBackhaulRuleRequiredCodeRule("capped backhaul rule needs this field"),
            CappedBackhaulRuleCreatedAtRangeRule(120, 1000),
            CappedBackhaulRuleLimitRangeRule(32, 64),
            CappedBackhaulRuleDescriptionLengthRule(255),
            CappedBackhaulRuleCodePrefixRule("REF"),
        ),
    )
}
