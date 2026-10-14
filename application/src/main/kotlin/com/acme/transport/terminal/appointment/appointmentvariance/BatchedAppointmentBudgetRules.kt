package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.text.normalize

/** A rule a batched appointment budget must pass. Check returns its problems, or none. */
interface BatchedAppointmentBudgetRule {
    fun check(item: BatchedAppointmentBudget): MutableList<String>
}

data class BatchedAppointmentBudgetAllowedCodeRule(
    var allowed: MutableList<String>,
) : BatchedAppointmentBudgetRule {
    override fun check(item: BatchedAppointmentBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAppointmentBudgetRequiredSourceRule(var message: String) : BatchedAppointmentBudgetRule {
    override fun check(item: BatchedAppointmentBudget): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAppointmentBudgetCurrencyLengthRule(var maxLength: Int) : BatchedAppointmentBudgetRule {
    override fun check(item: BatchedAppointmentBudget): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAppointmentBudgetCodePrefixRule(var prefix: String) : BatchedAppointmentBudgetRule {
    override fun check(item: BatchedAppointmentBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched appointment budget. */
data class BatchedAppointmentBudgetValidator(var rules: MutableList<BatchedAppointmentBudgetRule>) {
    fun validate(item: BatchedAppointmentBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedAppointmentBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedAppointmentBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedAppointmentBudgetValidator(): BatchedAppointmentBudgetValidator {
    return BatchedAppointmentBudgetValidator(
        mutableListOf<BatchedAppointmentBudgetRule>(
            BatchedAppointmentBudgetAllowedCodeRule(mutableListOf<String>("tiered", "seasonal")),
            BatchedAppointmentBudgetRequiredSourceRule("batched appointment budget needs this field"),
            BatchedAppointmentBudgetCurrencyLengthRule(120),
            BatchedAppointmentBudgetCodePrefixRule("X"),
        ),
    )
}
