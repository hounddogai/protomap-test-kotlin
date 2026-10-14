package com.acme.transport.freightpay.freightinvoice.quota

/** A rule a tiered freight invoice quota must pass. Check returns its problems, or none. */
interface TieredFreightInvoiceQuotaRule {
    fun check(item: TieredFreightInvoiceQuota): MutableList<String>
}

data class TieredFreightInvoiceQuotaLabelLengthRule(var maxLength: Int) : TieredFreightInvoiceQuotaRule {
    override fun check(item: TieredFreightInvoiceQuota): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredFreightInvoiceQuotaRequiredNameRule(var message: String) : TieredFreightInvoiceQuotaRule {
    override fun check(item: TieredFreightInvoiceQuota): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredFreightInvoiceQuotaRequiredCurrencyRule(var message: String) : TieredFreightInvoiceQuotaRule {
    override fun check(item: TieredFreightInvoiceQuota): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredFreightInvoiceQuotaCodePrefixRule(var prefix: String) : TieredFreightInvoiceQuotaRule {
    override fun check(item: TieredFreightInvoiceQuota): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredFreightInvoiceQuotaAmountRangeRule(
    var min: Double,
    var max: Double,
) : TieredFreightInvoiceQuotaRule {
    override fun check(item: TieredFreightInvoiceQuota): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tiered freight invoice quota. */
data class TieredFreightInvoiceQuotaValidator(var rules: MutableList<TieredFreightInvoiceQuotaRule>) {
    fun validate(item: TieredFreightInvoiceQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredFreightInvoiceQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredFreightInvoiceQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredFreightInvoiceQuotaValidator(): TieredFreightInvoiceQuotaValidator {
    return TieredFreightInvoiceQuotaValidator(
        mutableListOf<TieredFreightInvoiceQuotaRule>(
            TieredFreightInvoiceQuotaLabelLengthRule(86400),
            TieredFreightInvoiceQuotaRequiredNameRule("tiered freight invoice quota needs this field"),
            TieredFreightInvoiceQuotaRequiredCurrencyRule("tiered freight invoice quota needs this field"),
            TieredFreightInvoiceQuotaCodePrefixRule("Q"),
            TieredFreightInvoiceQuotaAmountRangeRule(4.1, 4381.1),
        ),
    )
}
