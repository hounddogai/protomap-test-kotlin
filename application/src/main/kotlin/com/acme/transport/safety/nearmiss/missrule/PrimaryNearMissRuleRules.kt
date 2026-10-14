package com.acme.transport.safety.nearmiss.missrule

import com.acme.transport.kit.text.normalize

/** A rule a primary near miss rule must pass. Check returns its problems, or none. */
interface PrimaryNearMissRuleRule {
    fun check(item: PrimaryNearMissRule): MutableList<String>
}

data class PrimaryNearMissRuleCreatedAtRangeRule(var min: Int, var max: Int) : PrimaryNearMissRuleRule {
    override fun check(item: PrimaryNearMissRule): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class PrimaryNearMissRuleRequiredTitleRule(var message: String) : PrimaryNearMissRuleRule {
    override fun check(item: PrimaryNearMissRule): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNearMissRuleAllowedCodeRule(var allowed: MutableList<String>) : PrimaryNearMissRuleRule {
    override fun check(item: PrimaryNearMissRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNearMissRuleRequiredCodeRule(var message: String) : PrimaryNearMissRuleRule {
    override fun check(item: PrimaryNearMissRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNearMissRuleRequiredRegionRule(var message: String) : PrimaryNearMissRuleRule {
    override fun check(item: PrimaryNearMissRule): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNearMissRuleTitleLengthRule(var maxLength: Int) : PrimaryNearMissRuleRule {
    override fun check(item: PrimaryNearMissRule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary near miss rule. */
data class PrimaryNearMissRuleValidator(var rules: MutableList<PrimaryNearMissRuleRule>) {
    fun validate(item: PrimaryNearMissRule): MutableList<String> {
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

    fun requireValid(item: PrimaryNearMissRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryNearMissRule>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryNearMissRuleValidator(): PrimaryNearMissRuleValidator {
    return PrimaryNearMissRuleValidator(
        mutableListOf<PrimaryNearMissRuleRule>(
            PrimaryNearMissRuleCreatedAtRangeRule(16, 86400),
            PrimaryNearMissRuleRequiredTitleRule("primary near miss rule needs a value"),
            PrimaryNearMissRuleAllowedCodeRule(mutableListOf<String>("default")),
            PrimaryNearMissRuleRequiredCodeRule("primary near miss rule needs this field"),
            PrimaryNearMissRuleRequiredRegionRule("primary near miss rule needs an entry"),
            PrimaryNearMissRuleTitleLengthRule(3600),
        ),
    )
}
