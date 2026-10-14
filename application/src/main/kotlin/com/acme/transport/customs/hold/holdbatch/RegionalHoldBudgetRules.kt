package com.acme.transport.customs.hold.holdbatch

/** A rule a regional hold budget must pass. Check returns its problems, or none. */
interface RegionalHoldBudgetRule {
    fun check(item: RegionalHoldBudget): MutableList<String>
}

data class RegionalHoldBudgetStopCountRangeRule(var min: Int, var max: Int) : RegionalHoldBudgetRule {
    override fun check(item: RegionalHoldBudget): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalHoldBudgetCodeLengthRule(var maxLength: Int) : RegionalHoldBudgetRule {
    override fun check(item: RegionalHoldBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalHoldBudgetCodePrefixRule(var prefix: String) : RegionalHoldBudgetRule {
    override fun check(item: RegionalHoldBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalHoldBudgetRequiredNameRule(var message: String) : RegionalHoldBudgetRule {
    override fun check(item: RegionalHoldBudget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional hold budget. */
data class RegionalHoldBudgetValidator(var rules: MutableList<RegionalHoldBudgetRule>) {
    fun validate(item: RegionalHoldBudget): MutableList<String> {
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

    fun requireValid(item: RegionalHoldBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalHoldBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalHoldBudgetValidator(): RegionalHoldBudgetValidator {
    return RegionalHoldBudgetValidator(
        mutableListOf<RegionalHoldBudgetRule>(
            RegionalHoldBudgetStopCountRangeRule(80, 86400),
            RegionalHoldBudgetCodeLengthRule(120),
            RegionalHoldBudgetCodePrefixRule("OPS"),
            RegionalHoldBudgetRequiredNameRule("regional hold budget needs an entry"),
        ),
    )
}
