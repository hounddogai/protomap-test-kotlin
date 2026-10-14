package com.acme.transport.freightpay.freightinvoice.invoiceadjustment

/** A rule a primary freight invoice adjustment must pass. Check returns its problems, or none. */
interface PrimaryFreightInvoiceAdjustmentRule {
    fun check(item: PrimaryFreightInvoiceAdjustment): MutableList<String>
}

data class PrimaryFreightInvoiceAdjustmentRequiredTitleRule(
    var message: String,
) : PrimaryFreightInvoiceAdjustmentRule {
    override fun check(item: PrimaryFreightInvoiceAdjustment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFreightInvoiceAdjustmentStopCountRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryFreightInvoiceAdjustmentRule {
    override fun check(item: PrimaryFreightInvoiceAdjustment): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFreightInvoiceAdjustmentCurrencyLengthRule(
    var maxLength: Int,
) : PrimaryFreightInvoiceAdjustmentRule {
    override fun check(item: PrimaryFreightInvoiceAdjustment): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary freight invoice adjustment. */
data class PrimaryFreightInvoiceAdjustmentValidator(
    var rules: MutableList<PrimaryFreightInvoiceAdjustmentRule>,
) {
    fun validate(item: PrimaryFreightInvoiceAdjustment): MutableList<String> {
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

    fun requireValid(item: PrimaryFreightInvoiceAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryFreightInvoiceAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryFreightInvoiceAdjustmentValidator(): PrimaryFreightInvoiceAdjustmentValidator {
    return PrimaryFreightInvoiceAdjustmentValidator(
        mutableListOf<PrimaryFreightInvoiceAdjustmentRule>(
            PrimaryFreightInvoiceAdjustmentRequiredTitleRule(
                "primary freight invoice adjustment needs an entry",
            ),
            PrimaryFreightInvoiceAdjustmentStopCountRangeRule(32, 120),
            PrimaryFreightInvoiceAdjustmentCurrencyLengthRule(86400),
        ),
    )
}
