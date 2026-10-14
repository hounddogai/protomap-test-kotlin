package com.acme.transport.freightpay.audit.auditpolicy

/** A rule a legacy audit rule must pass. Check returns its problems, or none. */
interface LegacyAuditRuleRule {
    fun check(item: LegacyAuditRule): MutableList<String>
}

data class LegacyAuditRuleCeilingRangeRule(var min: Double, var max: Double) : LegacyAuditRuleRule {
    override fun check(item: LegacyAuditRule): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyAuditRuleTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : LegacyAuditRuleRule {
    override fun check(item: LegacyAuditRule): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class LegacyAuditRuleNotesLengthRule(var maxLength: Int) : LegacyAuditRuleRule {
    override fun check(item: LegacyAuditRule): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy audit rule. */
data class LegacyAuditRuleValidator(var rules: MutableList<LegacyAuditRuleRule>) {
    fun validate(item: LegacyAuditRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyAuditRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyAuditRule>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyAuditRuleValidator(): LegacyAuditRuleValidator {
    return LegacyAuditRuleValidator(
        mutableListOf<LegacyAuditRuleRule>(
            LegacyAuditRuleCeilingRangeRule(1.6, 3922.2),
            LegacyAuditRuleTagsLimitRule(255, mutableListOf<String>("adaptive")),
            LegacyAuditRuleNotesLengthRule(16),
        ),
    )
}
