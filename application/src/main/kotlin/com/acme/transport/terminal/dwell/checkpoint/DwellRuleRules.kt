package com.acme.transport.terminal.dwell.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a dwell rule must pass. Check returns its problems, or none. */
interface DwellRuleRule {
    fun check(item: DwellRule): MutableList<String>
}

data class DwellRuleRequiredSummaryRule(var message: String) : DwellRuleRule {
    override fun check(item: DwellRule): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellRuleAllowedDockRule(var allowed: MutableList<String>) : DwellRuleRule {
    override fun check(item: DwellRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is stale")
        }
        return mutableListOf<String>()
    }
}

data class DwellRuleStopCountRangeRule(var min: Int, var max: Int) : DwellRuleRule {
    override fun check(item: DwellRule): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class DwellRuleCodeLengthRule(var maxLength: Int) : DwellRuleRule {
    override fun check(item: DwellRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellRuleRequiredNameRule(var message: String) : DwellRuleRule {
    override fun check(item: DwellRule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellRuleOdometerRangeRule(var min: Int, var max: Int) : DwellRuleRule {
    override fun check(item: DwellRule): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell rule. */
data class DwellRuleValidator(var rules: MutableList<DwellRuleRule>) {
    fun validate(item: DwellRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DwellRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellRule>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellRuleValidator(): DwellRuleValidator {
    return DwellRuleValidator(
        mutableListOf<DwellRuleRule>(
            DwellRuleRequiredSummaryRule("dwell rule needs this field"),
            DwellRuleAllowedDockRule(mutableListOf<String>()),
            DwellRuleStopCountRangeRule(16, 64),
            DwellRuleCodeLengthRule(86400),
            DwellRuleRequiredNameRule("dwell rule needs an entry"),
            DwellRuleOdometerRangeRule(64, 1000),
        ),
    )
}
