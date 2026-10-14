package com.acme.transport.terminal.dwell.template

import com.acme.transport.kit.text.normalize

/** A rule a dwell budget must pass. Check returns its problems, or none. */
interface DwellBudgetRule {
    fun check(item: DwellBudget): MutableList<String>
}

data class DwellBudgetContactEmailLengthRule(var maxLength: Int) : DwellBudgetRule {
    override fun check(item: DwellBudget): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellBudgetRequiredCodeRule(var message: String) : DwellBudgetRule {
    override fun check(item: DwellBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellBudgetCodePrefixRule(var prefix: String) : DwellBudgetRule {
    override fun check(item: DwellBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DwellBudgetAllowedContactEmailRule(var allowed: MutableList<String>) : DwellBudgetRule {
    override fun check(item: DwellBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell budget. */
data class DwellBudgetValidator(var rules: MutableList<DwellBudgetRule>) {
    fun validate(item: DwellBudget): MutableList<String> {
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

    fun requireValid(item: DwellBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellBudgetValidator(): DwellBudgetValidator {
    return DwellBudgetValidator(
        mutableListOf<DwellBudgetRule>(
            DwellBudgetContactEmailLengthRule(64),
            DwellBudgetRequiredCodeRule("dwell budget needs a value"),
            DwellBudgetCodePrefixRule("ACM"),
            DwellBudgetAllowedContactEmailRule(mutableListOf<String>()),
        ),
    )
}
