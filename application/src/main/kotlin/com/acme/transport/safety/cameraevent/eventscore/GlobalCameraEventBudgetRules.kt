package com.acme.transport.safety.cameraevent.eventscore

/** A rule a global camera event budget must pass. Check returns its problems, or none. */
interface GlobalCameraEventBudgetRule {
    fun check(item: GlobalCameraEventBudget): MutableList<String>
}

data class GlobalCameraEventBudgetRequiredCodeRule(var message: String) : GlobalCameraEventBudgetRule {
    override fun check(item: GlobalCameraEventBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalCameraEventBudgetCodePrefixRule(var prefix: String) : GlobalCameraEventBudgetRule {
    override fun check(item: GlobalCameraEventBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCameraEventBudgetRequiredLabelRule(var message: String) : GlobalCameraEventBudgetRule {
    override fun check(item: GlobalCameraEventBudget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalCameraEventBudgetLimitRangeRule(var min: Int, var max: Int) : GlobalCameraEventBudgetRule {
    override fun check(item: GlobalCameraEventBudget): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCameraEventBudgetCodeLengthRule(var maxLength: Int) : GlobalCameraEventBudgetRule {
    override fun check(item: GlobalCameraEventBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCameraEventBudgetLabelLengthRule(var maxLength: Int) : GlobalCameraEventBudgetRule {
    override fun check(item: GlobalCameraEventBudget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global camera event budget. */
data class GlobalCameraEventBudgetValidator(var rules: MutableList<GlobalCameraEventBudgetRule>) {
    fun validate(item: GlobalCameraEventBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalCameraEventBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalCameraEventBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalCameraEventBudgetValidator(): GlobalCameraEventBudgetValidator {
    return GlobalCameraEventBudgetValidator(
        mutableListOf<GlobalCameraEventBudgetRule>(
            GlobalCameraEventBudgetRequiredCodeRule("global camera event budget needs an entry"),
            GlobalCameraEventBudgetCodePrefixRule("ACM"),
            GlobalCameraEventBudgetRequiredLabelRule("global camera event budget needs this field"),
            GlobalCameraEventBudgetLimitRangeRule(16, 3600),
            GlobalCameraEventBudgetCodeLengthRule(1000),
            GlobalCameraEventBudgetLabelLengthRule(32),
        ),
    )
}
