package com.acme.transport.crewing.roster.rule

/** A rule a roster rule must pass. Check returns its problems, or none. */
interface RosterRuleRule {
    fun check(item: RosterRule): MutableList<String>
}

data class RosterRuleLimitRangeRule(var min: Int, var max: Int) : RosterRuleRule {
    override fun check(item: RosterRule): MutableList<String> {
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

data class RosterRuleCodePrefixRule(var prefix: String) : RosterRuleRule {
    override fun check(item: RosterRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RosterRuleExternalRefLengthRule(var maxLength: Int) : RosterRuleRule {
    override fun check(item: RosterRule): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterRuleCodeLengthRule(var maxLength: Int) : RosterRuleRule {
    override fun check(item: RosterRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterRuleRequiredRouteCodeRule(var message: String) : RosterRuleRule {
    override fun check(item: RosterRule): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterRuleRouteCodeLengthRule(var maxLength: Int) : RosterRuleRule {
    override fun check(item: RosterRule): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster rule. */
data class RosterRuleValidator(var rules: MutableList<RosterRuleRule>) {
    fun validate(item: RosterRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RosterRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterRule>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterRuleValidator(): RosterRuleValidator {
    return RosterRuleValidator(
        mutableListOf<RosterRuleRule>(
            RosterRuleLimitRangeRule(32, 1000),
            RosterRuleCodePrefixRule("ACM"),
            RosterRuleExternalRefLengthRule(86400),
            RosterRuleCodeLengthRule(86400),
            RosterRuleRequiredRouteCodeRule("roster rule needs this field"),
            RosterRuleRouteCodeLengthRule(86400),
        ),
    )
}
