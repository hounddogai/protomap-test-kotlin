package com.acme.transport.network.servicearea.cycle

/** A rule a regional service area budget must pass. Check returns its problems, or none. */
interface RegionalServiceAreaBudgetRule {
    fun check(item: RegionalServiceAreaBudget): MutableList<String>
}

data class RegionalServiceAreaBudgetCodeLengthRule(var maxLength: Int) : RegionalServiceAreaBudgetRule {
    override fun check(item: RegionalServiceAreaBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalServiceAreaBudgetSequenceRangeRule(
    var min: Int,
    var max: Int,
) : RegionalServiceAreaBudgetRule {
    override fun check(item: RegionalServiceAreaBudget): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class RegionalServiceAreaBudgetLabelLengthRule(var maxLength: Int) : RegionalServiceAreaBudgetRule {
    override fun check(item: RegionalServiceAreaBudget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalServiceAreaBudgetCodePrefixRule(var prefix: String) : RegionalServiceAreaBudgetRule {
    override fun check(item: RegionalServiceAreaBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional service area budget. */
data class RegionalServiceAreaBudgetValidator(var rules: MutableList<RegionalServiceAreaBudgetRule>) {
    fun validate(item: RegionalServiceAreaBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalServiceAreaBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalServiceAreaBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalServiceAreaBudgetValidator(): RegionalServiceAreaBudgetValidator {
    return RegionalServiceAreaBudgetValidator(
        mutableListOf<RegionalServiceAreaBudgetRule>(
            RegionalServiceAreaBudgetCodeLengthRule(120),
            RegionalServiceAreaBudgetSequenceRangeRule(64, 120),
            RegionalServiceAreaBudgetLabelLengthRule(3600),
            RegionalServiceAreaBudgetCodePrefixRule("REF"),
        ),
    )
}
