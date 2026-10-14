package com.acme.transport.fleetops.utilization.target

/** A rule a utilization rule must pass. Check returns its problems, or none. */
interface UtilizationRuleRule {
    fun check(item: UtilizationRule): MutableList<String>
}

data class UtilizationRuleRequiredCodeRule(var message: String) : UtilizationRuleRule {
    override fun check(item: UtilizationRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationRuleCodeLengthRule(var maxLength: Int) : UtilizationRuleRule {
    override fun check(item: UtilizationRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationRuleDockLengthRule(var maxLength: Int) : UtilizationRuleRule {
    override fun check(item: UtilizationRule): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization rule. */
data class UtilizationRuleValidator(var rules: MutableList<UtilizationRuleRule>) {
    fun validate(item: UtilizationRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationRule>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationRuleValidator(): UtilizationRuleValidator {
    return UtilizationRuleValidator(
        mutableListOf<UtilizationRuleRule>(
            UtilizationRuleRequiredCodeRule("utilization rule needs a value"),
            UtilizationRuleCodeLengthRule(120),
            UtilizationRuleDockLengthRule(16),
        ),
    )
}
