package com.acme.transport.freightpay.freightinvoice.invoicereview

/** A rule a tiered freight invoice variance must pass. Check returns its problems, or none. */
interface TieredFreightInvoiceVarianceRule {
    fun check(item: TieredFreightInvoiceVariance): MutableList<String>
}

data class TieredFreightInvoiceVarianceCapacityRangeRule(
    var min: Int,
    var max: Int,
) : TieredFreightInvoiceVarianceRule {
    override fun check(item: TieredFreightInvoiceVariance): MutableList<String> {
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

data class TieredFreightInvoiceVarianceCodePrefixRule(var prefix: String) : TieredFreightInvoiceVarianceRule {
    override fun check(item: TieredFreightInvoiceVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredFreightInvoiceVarianceRequiredSummaryRule(
    var message: String,
) : TieredFreightInvoiceVarianceRule {
    override fun check(item: TieredFreightInvoiceVariance): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered freight invoice variance. */
data class TieredFreightInvoiceVarianceValidator(var rules: MutableList<TieredFreightInvoiceVarianceRule>) {
    fun validate(item: TieredFreightInvoiceVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredFreightInvoiceVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredFreightInvoiceVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredFreightInvoiceVarianceValidator(): TieredFreightInvoiceVarianceValidator {
    return TieredFreightInvoiceVarianceValidator(
        mutableListOf<TieredFreightInvoiceVarianceRule>(
            TieredFreightInvoiceVarianceCapacityRangeRule(3600, 3600),
            TieredFreightInvoiceVarianceCodePrefixRule("ACM"),
            TieredFreightInvoiceVarianceRequiredSummaryRule("tiered freight invoice variance needs a value"),
        ),
    )
}
