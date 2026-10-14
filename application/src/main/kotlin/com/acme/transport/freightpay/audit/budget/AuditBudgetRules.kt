package com.acme.transport.freightpay.audit.budget

import com.acme.transport.kit.text.normalize

/** A rule a audit budget must pass. Check returns its problems, or none. */
interface AuditBudgetRule {
    fun check(item: AuditBudget): MutableList<String>
}

data class AuditBudgetAllowedTitleRule(var allowed: MutableList<String>) : AuditBudgetRule {
    override fun check(item: AuditBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class AuditBudgetCodePrefixRule(var prefix: String) : AuditBudgetRule {
    override fun check(item: AuditBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AuditBudgetStopCountRangeRule(var min: Int, var max: Int) : AuditBudgetRule {
    override fun check(item: AuditBudget): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AuditBudgetRequiredTitleRule(var message: String) : AuditBudgetRule {
    override fun check(item: AuditBudget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit budget. */
data class AuditBudgetValidator(var rules: MutableList<AuditBudgetRule>) {
    fun validate(item: AuditBudget): MutableList<String> {
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

    fun requireValid(item: AuditBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditBudgetValidator(): AuditBudgetValidator {
    return AuditBudgetValidator(
        mutableListOf<AuditBudgetRule>(
            AuditBudgetAllowedTitleRule(mutableListOf<String>("legacy", "strict")),
            AuditBudgetCodePrefixRule("OPS"),
            AuditBudgetStopCountRangeRule(64, 120),
            AuditBudgetRequiredTitleRule("audit budget needs a value"),
        ),
    )
}
