package com.acme.transport.freightpay.freightinvoice.invoicemanifest

/** A rule a weighted freight invoice policy must pass. Check returns its problems, or none. */
interface WeightedFreightInvoicePolicyRule {
    fun check(item: WeightedFreightInvoicePolicy): MutableList<String>
}

data class WeightedFreightInvoicePolicyDockLengthRule(var maxLength: Int) : WeightedFreightInvoicePolicyRule {
    override fun check(item: WeightedFreightInvoicePolicy): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedFreightInvoicePolicyUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : WeightedFreightInvoicePolicyRule {
    override fun check(item: WeightedFreightInvoicePolicy): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class WeightedFreightInvoicePolicyQuantityRangeRule(
    var min: Int,
    var max: Int,
) : WeightedFreightInvoicePolicyRule {
    override fun check(item: WeightedFreightInvoicePolicy): MutableList<String> {
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

/** Runs every rule of a weighted freight invoice policy. */
data class WeightedFreightInvoicePolicyValidator(var rules: MutableList<WeightedFreightInvoicePolicyRule>) {
    fun validate(item: WeightedFreightInvoicePolicy): MutableList<String> {
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

    fun requireValid(item: WeightedFreightInvoicePolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedFreightInvoicePolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedFreightInvoicePolicyValidator(): WeightedFreightInvoicePolicyValidator {
    return WeightedFreightInvoicePolicyValidator(
        mutableListOf<WeightedFreightInvoicePolicyRule>(
            WeightedFreightInvoicePolicyDockLengthRule(120),
            WeightedFreightInvoicePolicyUpdatedAtRangeRule(80, 3600),
            WeightedFreightInvoicePolicyQuantityRangeRule(16, 1000),
        ),
    )
}
