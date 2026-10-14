package com.acme.transport.lastmile.drop.dropreservation

/** A rule a drop rule must pass. Check returns its problems, or none. */
interface DropRuleRule {
    fun check(item: DropRule): MutableList<String>
}

data class DropRuleArchivedGuardRule(var limit: Int) : DropRuleRule {
    override fun check(item: DropRule): MutableList<String> {
        if (item.archived && item.priority > limit) {
            return mutableListOf<String>("priority ${item.priority} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class DropRuleRequiredPhoneRule(var message: String) : DropRuleRule {
    override fun check(item: DropRule): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropRuleCodePrefixRule(var prefix: String) : DropRuleRule {
    override fun check(item: DropRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DropRulePhoneLengthRule(var maxLength: Int) : DropRuleRule {
    override fun check(item: DropRule): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropRulePriorityRangeRule(var min: Int, var max: Int) : DropRuleRule {
    override fun check(item: DropRule): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a drop rule. */
data class DropRuleValidator(var rules: MutableList<DropRuleRule>) {
    fun validate(item: DropRule): MutableList<String> {
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

    fun requireValid(item: DropRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropRule>): MutableMap<String, MutableList<String>> {
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

fun defaultDropRuleValidator(): DropRuleValidator {
    return DropRuleValidator(
        mutableListOf<DropRuleRule>(
            DropRuleArchivedGuardRule(32),
            DropRuleRequiredPhoneRule("drop rule needs this field"),
            DropRuleCodePrefixRule("Q"),
            DropRulePhoneLengthRule(32),
            DropRulePriorityRangeRule(64, 3600),
        ),
    )
}
