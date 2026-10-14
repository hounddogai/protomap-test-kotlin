package com.acme.transport.linehaul.relay.relaythreshold

/** A rule a legacy relay rule must pass. Check returns its problems, or none. */
interface LegacyRelayRuleRule {
    fun check(item: LegacyRelayRule): MutableList<String>
}

data class LegacyRelayRulePriorityRangeRule(var min: Int, var max: Int) : LegacyRelayRuleRule {
    override fun check(item: LegacyRelayRule): MutableList<String> {
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

data class LegacyRelayRuleCodeLengthRule(var maxLength: Int) : LegacyRelayRuleRule {
    override fun check(item: LegacyRelayRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRelayRuleRequiredCodeRule(var message: String) : LegacyRelayRuleRule {
    override fun check(item: LegacyRelayRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyRelayRuleRequiredTitleRule(var message: String) : LegacyRelayRuleRule {
    override fun check(item: LegacyRelayRule): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyRelayRuleRequiredLaneRule(var message: String) : LegacyRelayRuleRule {
    override fun check(item: LegacyRelayRule): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyRelayRuleRevisionRangeRule(var min: Int, var max: Int) : LegacyRelayRuleRule {
    override fun check(item: LegacyRelayRule): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a legacy relay rule. */
data class LegacyRelayRuleValidator(var rules: MutableList<LegacyRelayRuleRule>) {
    fun validate(item: LegacyRelayRule): MutableList<String> {
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

    fun requireValid(item: LegacyRelayRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyRelayRule>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyRelayRuleValidator(): LegacyRelayRuleValidator {
    return LegacyRelayRuleValidator(
        mutableListOf<LegacyRelayRuleRule>(
            LegacyRelayRulePriorityRangeRule(32, 64),
            LegacyRelayRuleCodeLengthRule(255),
            LegacyRelayRuleRequiredCodeRule("legacy relay rule needs this field"),
            LegacyRelayRuleRequiredTitleRule("legacy relay rule needs an entry"),
            LegacyRelayRuleRequiredLaneRule("legacy relay rule needs an entry"),
            LegacyRelayRuleRevisionRangeRule(32, 64),
        ),
    )
}
