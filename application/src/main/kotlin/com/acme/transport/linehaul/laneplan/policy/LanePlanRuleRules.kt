package com.acme.transport.linehaul.laneplan.policy

/** A rule a lane plan rule must pass. Check returns its problems, or none. */
interface LanePlanRuleRule {
    fun check(item: LanePlanRule): MutableList<String>
}

data class LanePlanRuleRequiredNameRule(var message: String) : LanePlanRuleRule {
    override fun check(item: LanePlanRule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanRuleRequiredCarrierRule(var message: String) : LanePlanRuleRule {
    override fun check(item: LanePlanRule): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanRuleNameLengthRule(var maxLength: Int) : LanePlanRuleRule {
    override fun check(item: LanePlanRule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanRuleCodePrefixRule(var prefix: String) : LanePlanRuleRule {
    override fun check(item: LanePlanRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan rule. */
data class LanePlanRuleValidator(var rules: MutableList<LanePlanRuleRule>) {
    fun validate(item: LanePlanRule): MutableList<String> {
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

    fun requireValid(item: LanePlanRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanRule>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanRuleValidator(): LanePlanRuleValidator {
    return LanePlanRuleValidator(
        mutableListOf<LanePlanRuleRule>(
            LanePlanRuleRequiredNameRule("lane plan rule needs this field"),
            LanePlanRuleRequiredCarrierRule("lane plan rule needs an entry"),
            LanePlanRuleNameLengthRule(120),
            LanePlanRuleCodePrefixRule("X"),
        ),
    )
}
