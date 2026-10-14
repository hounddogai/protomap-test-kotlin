package com.acme.transport.customs.inspection.rule

/** A rule a inspection budget must pass. Check returns its problems, or none. */
interface InspectionBudgetRule {
    fun check(item: InspectionBudget): MutableList<String>
}

data class InspectionBudgetNameLengthRule(var maxLength: Int) : InspectionBudgetRule {
    override fun check(item: InspectionBudget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionBudgetLabelLengthRule(var maxLength: Int) : InspectionBudgetRule {
    override fun check(item: InspectionBudget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionBudgetAmountRangeRule(var min: Double, var max: Double) : InspectionBudgetRule {
    override fun check(item: InspectionBudget): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionBudgetRequiredNameRule(var message: String) : InspectionBudgetRule {
    override fun check(item: InspectionBudget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionBudgetRequiredCodeRule(var message: String) : InspectionBudgetRule {
    override fun check(item: InspectionBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionBudgetRouteCodeLengthRule(var maxLength: Int) : InspectionBudgetRule {
    override fun check(item: InspectionBudget): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection budget. */
data class InspectionBudgetValidator(var rules: MutableList<InspectionBudgetRule>) {
    fun validate(item: InspectionBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionBudgetValidator(): InspectionBudgetValidator {
    return InspectionBudgetValidator(
        mutableListOf<InspectionBudgetRule>(
            InspectionBudgetNameLengthRule(80),
            InspectionBudgetLabelLengthRule(86400),
            InspectionBudgetAmountRangeRule(7.2, 3877.7),
            InspectionBudgetRequiredNameRule("inspection budget needs this field"),
            InspectionBudgetRequiredCodeRule("inspection budget needs an entry"),
            InspectionBudgetRouteCodeLengthRule(3600),
        ),
    )
}
