package com.acme.transport.freightpay.freightinvoice.invoicequota

/** A rule a rolling freight invoice quota must pass. Check returns its problems, or none. */
interface RollingFreightInvoiceQuotaRule {
    fun check(item: RollingFreightInvoiceQuota): MutableList<String>
}

data class RollingFreightInvoiceQuotaRequiredNameRule(var message: String) : RollingFreightInvoiceQuotaRule {
    override fun check(item: RollingFreightInvoiceQuota): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingFreightInvoiceQuotaCodeLengthRule(var maxLength: Int) : RollingFreightInvoiceQuotaRule {
    override fun check(item: RollingFreightInvoiceQuota): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingFreightInvoiceQuotaWeightRangeRule(
    var min: Double,
    var max: Double,
) : RollingFreightInvoiceQuotaRule {
    override fun check(item: RollingFreightInvoiceQuota): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingFreightInvoiceQuotaNameLengthRule(var maxLength: Int) : RollingFreightInvoiceQuotaRule {
    override fun check(item: RollingFreightInvoiceQuota): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling freight invoice quota. */
data class RollingFreightInvoiceQuotaValidator(var rules: MutableList<RollingFreightInvoiceQuotaRule>) {
    fun validate(item: RollingFreightInvoiceQuota): MutableList<String> {
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

    fun requireValid(item: RollingFreightInvoiceQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingFreightInvoiceQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingFreightInvoiceQuotaValidator(): RollingFreightInvoiceQuotaValidator {
    return RollingFreightInvoiceQuotaValidator(
        mutableListOf<RollingFreightInvoiceQuotaRule>(
            RollingFreightInvoiceQuotaRequiredNameRule("rolling freight invoice quota needs this field"),
            RollingFreightInvoiceQuotaCodeLengthRule(86400),
            RollingFreightInvoiceQuotaWeightRangeRule(0.8, 1784.3),
            RollingFreightInvoiceQuotaNameLengthRule(64),
        ),
    )
}
