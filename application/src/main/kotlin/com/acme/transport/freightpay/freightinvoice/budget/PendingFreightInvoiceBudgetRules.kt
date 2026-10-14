package com.acme.transport.freightpay.freightinvoice.budget

/** A rule a pending freight invoice budget must pass. Check returns its problems, or none. */
interface PendingFreightInvoiceBudgetRule {
    fun check(item: PendingFreightInvoiceBudget): MutableList<String>
}

data class PendingFreightInvoiceBudgetRequiredCodeRule(var message: String) : PendingFreightInvoiceBudgetRule {
    override fun check(item: PendingFreightInvoiceBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingFreightInvoiceBudgetThresholdRangeRule(
    var min: Double,
    var max: Double,
) : PendingFreightInvoiceBudgetRule {
    override fun check(item: PendingFreightInvoiceBudget): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingFreightInvoiceBudgetMarginRangeRule(
    var min: Double,
    var max: Double,
) : PendingFreightInvoiceBudgetRule {
    override fun check(item: PendingFreightInvoiceBudget): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingFreightInvoiceBudgetCodePrefixRule(var prefix: String) : PendingFreightInvoiceBudgetRule {
    override fun check(item: PendingFreightInvoiceBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending freight invoice budget. */
data class PendingFreightInvoiceBudgetValidator(var rules: MutableList<PendingFreightInvoiceBudgetRule>) {
    fun validate(item: PendingFreightInvoiceBudget): MutableList<String> {
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

    fun requireValid(item: PendingFreightInvoiceBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingFreightInvoiceBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingFreightInvoiceBudgetValidator(): PendingFreightInvoiceBudgetValidator {
    return PendingFreightInvoiceBudgetValidator(
        mutableListOf<PendingFreightInvoiceBudgetRule>(
            PendingFreightInvoiceBudgetRequiredCodeRule("pending freight invoice budget needs this field"),
            PendingFreightInvoiceBudgetThresholdRangeRule(7.0, 767.9),
            PendingFreightInvoiceBudgetMarginRangeRule(9.0, 1742.8),
            PendingFreightInvoiceBudgetCodePrefixRule("REF"),
        ),
    )
}
