package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.kit.text.normalize

/** A rule a primary roster rule must pass. Check returns its problems, or none. */
interface PrimaryRosterRuleRule {
    fun check(item: PrimaryRosterRule): MutableList<String>
}

data class PrimaryRosterRuleRequiredCodeRule(var message: String) : PrimaryRosterRuleRule {
    override fun check(item: PrimaryRosterRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRosterRuleRequiredTimezoneRule(var message: String) : PrimaryRosterRuleRule {
    override fun check(item: PrimaryRosterRule): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRosterRuleAllowedTimezoneRule(var allowed: MutableList<String>) : PrimaryRosterRuleRule {
    override fun check(item: PrimaryRosterRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRosterRuleCategoryLengthRule(var maxLength: Int) : PrimaryRosterRuleRule {
    override fun check(item: PrimaryRosterRule): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary roster rule. */
data class PrimaryRosterRuleValidator(var rules: MutableList<PrimaryRosterRuleRule>) {
    fun validate(item: PrimaryRosterRule): MutableList<String> {
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

    fun requireValid(item: PrimaryRosterRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryRosterRule>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryRosterRuleValidator(): PrimaryRosterRuleValidator {
    return PrimaryRosterRuleValidator(
        mutableListOf<PrimaryRosterRuleRule>(
            PrimaryRosterRuleRequiredCodeRule("primary roster rule needs a value"),
            PrimaryRosterRuleRequiredTimezoneRule("primary roster rule needs an entry"),
            PrimaryRosterRuleAllowedTimezoneRule(mutableListOf<String>("capped", "rolling")),
            PrimaryRosterRuleCategoryLengthRule(16),
        ),
    )
}
