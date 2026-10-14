package com.acme.transport.crewing.availability.availabilitydigest

/** A rule a manual availability budget must pass. Check returns its problems, or none. */
interface ManualAvailabilityBudgetRule {
    fun check(item: ManualAvailabilityBudget): MutableList<String>
}

data class ManualAvailabilityBudgetCodeLengthRule(var maxLength: Int) : ManualAvailabilityBudgetRule {
    override fun check(item: ManualAvailabilityBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualAvailabilityBudgetCategoryLengthRule(var maxLength: Int) : ManualAvailabilityBudgetRule {
    override fun check(item: ManualAvailabilityBudget): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualAvailabilityBudgetRequiredLabelRule(var message: String) : ManualAvailabilityBudgetRule {
    override fun check(item: ManualAvailabilityBudget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualAvailabilityBudgetCodePrefixRule(var prefix: String) : ManualAvailabilityBudgetRule {
    override fun check(item: ManualAvailabilityBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualAvailabilityBudgetPriorityRangeRule(
    var min: Int,
    var max: Int,
) : ManualAvailabilityBudgetRule {
    override fun check(item: ManualAvailabilityBudget): MutableList<String> {
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

/** Runs every rule of a manual availability budget. */
data class ManualAvailabilityBudgetValidator(var rules: MutableList<ManualAvailabilityBudgetRule>) {
    fun validate(item: ManualAvailabilityBudget): MutableList<String> {
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

    fun requireValid(item: ManualAvailabilityBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualAvailabilityBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultManualAvailabilityBudgetValidator(): ManualAvailabilityBudgetValidator {
    return ManualAvailabilityBudgetValidator(
        mutableListOf<ManualAvailabilityBudgetRule>(
            ManualAvailabilityBudgetCodeLengthRule(120),
            ManualAvailabilityBudgetCategoryLengthRule(86400),
            ManualAvailabilityBudgetRequiredLabelRule("manual availability budget needs this field"),
            ManualAvailabilityBudgetCodePrefixRule("Q"),
            ManualAvailabilityBudgetPriorityRangeRule(80, 86400),
        ),
    )
}
