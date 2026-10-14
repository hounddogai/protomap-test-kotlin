package com.acme.transport.terminal.dockdoor.rule

/** A rule a dock door rule must pass. Check returns its problems, or none. */
interface DockDoorRuleRule {
    fun check(item: DockDoorRule): MutableList<String>
}

data class DockDoorRuleRequiredCurrencyRule(var message: String) : DockDoorRuleRule {
    override fun check(item: DockDoorRule): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorRuleCodeLengthRule(var maxLength: Int) : DockDoorRuleRule {
    override fun check(item: DockDoorRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorRuleAttemptsRangeRule(var min: Int, var max: Int) : DockDoorRuleRule {
    override fun check(item: DockDoorRule): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorRuleRequiredNameRule(var message: String) : DockDoorRuleRule {
    override fun check(item: DockDoorRule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorRuleCodePrefixRule(var prefix: String) : DockDoorRuleRule {
    override fun check(item: DockDoorRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door rule. */
data class DockDoorRuleValidator(var rules: MutableList<DockDoorRuleRule>) {
    fun validate(item: DockDoorRule): MutableList<String> {
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

    fun requireValid(item: DockDoorRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorRule>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorRuleValidator(): DockDoorRuleValidator {
    return DockDoorRuleValidator(
        mutableListOf<DockDoorRuleRule>(
            DockDoorRuleRequiredCurrencyRule("dock door rule needs this field"),
            DockDoorRuleCodeLengthRule(120),
            DockDoorRuleAttemptsRangeRule(32, 32),
            DockDoorRuleRequiredNameRule("dock door rule needs a value"),
            DockDoorRuleCodePrefixRule("Q"),
        ),
    )
}
