package com.acme.transport.lastmile.redelivery.exception

/** A rule a manual redelivery budget must pass. Check returns its problems, or none. */
interface ManualRedeliveryBudgetRule {
    fun check(item: ManualRedeliveryBudget): MutableList<String>
}

data class ManualRedeliveryBudgetCodePrefixRule(var prefix: String) : ManualRedeliveryBudgetRule {
    override fun check(item: ManualRedeliveryBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualRedeliveryBudgetCodeLengthRule(var maxLength: Int) : ManualRedeliveryBudgetRule {
    override fun check(item: ManualRedeliveryBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualRedeliveryBudgetFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : ManualRedeliveryBudgetRule {
    override fun check(item: ManualRedeliveryBudget): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualRedeliveryBudgetExternalRefLengthRule(var maxLength: Int) : ManualRedeliveryBudgetRule {
    override fun check(item: ManualRedeliveryBudget): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual redelivery budget. */
data class ManualRedeliveryBudgetValidator(var rules: MutableList<ManualRedeliveryBudgetRule>) {
    fun validate(item: ManualRedeliveryBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualRedeliveryBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualRedeliveryBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultManualRedeliveryBudgetValidator(): ManualRedeliveryBudgetValidator {
    return ManualRedeliveryBudgetValidator(
        mutableListOf<ManualRedeliveryBudgetRule>(
            ManualRedeliveryBudgetCodePrefixRule("OPS"),
            ManualRedeliveryBudgetCodeLengthRule(120),
            ManualRedeliveryBudgetFuelLitersRangeRule(0.9, 3717.9),
            ManualRedeliveryBudgetExternalRefLengthRule(16),
        ),
    )
}
