package com.acme.transport.crewing.roster.rostermilestone

import com.acme.transport.kit.text.normalize

/** A rule a lenient roster rule must pass. Check returns its problems, or none. */
interface LenientRosterRuleRule {
    fun check(item: LenientRosterRule): MutableList<String>
}

data class LenientRosterRuleAllowedCodeRule(var allowed: MutableList<String>) : LenientRosterRuleRule {
    override fun check(item: LenientRosterRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class LenientRosterRuleTitleLengthRule(var maxLength: Int) : LenientRosterRuleRule {
    override fun check(item: LenientRosterRule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRosterRuleCodePrefixRule(var prefix: String) : LenientRosterRuleRule {
    override fun check(item: LenientRosterRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientRosterRuleRequiredContactEmailRule(var message: String) : LenientRosterRuleRule {
    override fun check(item: LenientRosterRule): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientRosterRuleCodeLengthRule(var maxLength: Int) : LenientRosterRuleRule {
    override fun check(item: LenientRosterRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRosterRuleActiveGuardRule(var limit: Int) : LenientRosterRuleRule {
    override fun check(item: LenientRosterRule): MutableList<String> {
        if (item.active && item.revision > limit) {
            return mutableListOf<String>("revision ${item.revision} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient roster rule. */
data class LenientRosterRuleValidator(var rules: MutableList<LenientRosterRuleRule>) {
    fun validate(item: LenientRosterRule): MutableList<String> {
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

    fun requireValid(item: LenientRosterRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRosterRule>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRosterRuleValidator(): LenientRosterRuleValidator {
    return LenientRosterRuleValidator(
        mutableListOf<LenientRosterRuleRule>(
            LenientRosterRuleAllowedCodeRule(mutableListOf<String>("capped", "weighted", "adaptive")),
            LenientRosterRuleTitleLengthRule(120),
            LenientRosterRuleCodePrefixRule("X"),
            LenientRosterRuleRequiredContactEmailRule("lenient roster rule needs this field"),
            LenientRosterRuleCodeLengthRule(32),
            LenientRosterRuleActiveGuardRule(64),
        ),
    )
}
