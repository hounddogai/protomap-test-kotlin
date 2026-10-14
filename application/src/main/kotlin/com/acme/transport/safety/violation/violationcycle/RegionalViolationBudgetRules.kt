package com.acme.transport.safety.violation.violationcycle

/** A rule a regional violation budget must pass. Check returns its problems, or none. */
interface RegionalViolationBudgetRule {
    fun check(item: RegionalViolationBudget): MutableList<String>
}

data class RegionalViolationBudgetCodePrefixRule(var prefix: String) : RegionalViolationBudgetRule {
    override fun check(item: RegionalViolationBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalViolationBudgetRequiredNameRule(var message: String) : RegionalViolationBudgetRule {
    override fun check(item: RegionalViolationBudget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalViolationBudgetRequiredOwnerIdRule(var message: String) : RegionalViolationBudgetRule {
    override fun check(item: RegionalViolationBudget): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalViolationBudgetOwnerIdLengthRule(var maxLength: Int) : RegionalViolationBudgetRule {
    override fun check(item: RegionalViolationBudget): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalViolationBudgetRequiredCodeRule(var message: String) : RegionalViolationBudgetRule {
    override fun check(item: RegionalViolationBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional violation budget. */
data class RegionalViolationBudgetValidator(var rules: MutableList<RegionalViolationBudgetRule>) {
    fun validate(item: RegionalViolationBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalViolationBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalViolationBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalViolationBudgetValidator(): RegionalViolationBudgetValidator {
    return RegionalViolationBudgetValidator(
        mutableListOf<RegionalViolationBudgetRule>(
            RegionalViolationBudgetCodePrefixRule("REF"),
            RegionalViolationBudgetRequiredNameRule("regional violation budget needs this field"),
            RegionalViolationBudgetRequiredOwnerIdRule("regional violation budget needs a value"),
            RegionalViolationBudgetOwnerIdLengthRule(32),
            RegionalViolationBudgetRequiredCodeRule("regional violation budget needs a value"),
        ),
    )
}
