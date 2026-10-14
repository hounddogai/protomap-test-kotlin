package com.acme.transport.terminal.crossdock.segment

/** A rule a cross dock rule must pass. Check returns its problems, or none. */
interface CrossDockRuleRule {
    fun check(item: CrossDockRule): MutableList<String>
}

data class CrossDockRuleRouteCodeLengthRule(var maxLength: Int) : CrossDockRuleRule {
    override fun check(item: CrossDockRule): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockRuleRequiredRouteCodeRule(var message: String) : CrossDockRuleRule {
    override fun check(item: CrossDockRule): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockRuleCodeLengthRule(var maxLength: Int) : CrossDockRuleRule {
    override fun check(item: CrossDockRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock rule. */
data class CrossDockRuleValidator(var rules: MutableList<CrossDockRuleRule>) {
    fun validate(item: CrossDockRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockRule>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockRuleValidator(): CrossDockRuleValidator {
    return CrossDockRuleValidator(
        mutableListOf<CrossDockRuleRule>(
            CrossDockRuleRouteCodeLengthRule(32),
            CrossDockRuleRequiredRouteCodeRule("cross dock rule needs this field"),
            CrossDockRuleCodeLengthRule(1000),
        ),
    )
}
