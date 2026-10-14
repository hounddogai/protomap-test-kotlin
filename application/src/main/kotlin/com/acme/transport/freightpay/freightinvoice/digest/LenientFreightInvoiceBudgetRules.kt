package com.acme.transport.freightpay.freightinvoice.digest

/** A rule a lenient freight invoice budget must pass. Check returns its problems, or none. */
interface LenientFreightInvoiceBudgetRule {
    fun check(item: LenientFreightInvoiceBudget): MutableList<String>
}

data class LenientFreightInvoiceBudgetCodePrefixRule(var prefix: String) : LenientFreightInvoiceBudgetRule {
    override fun check(item: LenientFreightInvoiceBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientFreightInvoiceBudgetCapacityRangeRule(
    var min: Int,
    var max: Int,
) : LenientFreightInvoiceBudgetRule {
    override fun check(item: LenientFreightInvoiceBudget): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class LenientFreightInvoiceBudgetRequiredCodeRule(var message: String) : LenientFreightInvoiceBudgetRule {
    override fun check(item: LenientFreightInvoiceBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientFreightInvoiceBudgetQuantityRangeRule(
    var min: Int,
    var max: Int,
) : LenientFreightInvoiceBudgetRule {
    override fun check(item: LenientFreightInvoiceBudget): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class LenientFreightInvoiceBudgetReviewerLengthRule(var maxLength: Int) : LenientFreightInvoiceBudgetRule {
    override fun check(item: LenientFreightInvoiceBudget): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientFreightInvoiceBudgetRequiredReviewerRule(
    var message: String,
) : LenientFreightInvoiceBudgetRule {
    override fun check(item: LenientFreightInvoiceBudget): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient freight invoice budget. */
data class LenientFreightInvoiceBudgetValidator(var rules: MutableList<LenientFreightInvoiceBudgetRule>) {
    fun validate(item: LenientFreightInvoiceBudget): MutableList<String> {
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

    fun requireValid(item: LenientFreightInvoiceBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientFreightInvoiceBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientFreightInvoiceBudgetValidator(): LenientFreightInvoiceBudgetValidator {
    return LenientFreightInvoiceBudgetValidator(
        mutableListOf<LenientFreightInvoiceBudgetRule>(
            LenientFreightInvoiceBudgetCodePrefixRule("REF"),
            LenientFreightInvoiceBudgetCapacityRangeRule(255, 255),
            LenientFreightInvoiceBudgetRequiredCodeRule("lenient freight invoice budget needs an entry"),
            LenientFreightInvoiceBudgetQuantityRangeRule(64, 64),
            LenientFreightInvoiceBudgetReviewerLengthRule(80),
            LenientFreightInvoiceBudgetRequiredReviewerRule("lenient freight invoice budget needs an entry"),
        ),
    )
}
