package com.acme.transport.freightpay.freightinvoice.notice

/** A rule a lenient freight invoice target must pass. Check returns its problems, or none. */
interface LenientFreightInvoiceTargetRule {
    fun check(item: LenientFreightInvoiceTarget): MutableList<String>
}

data class LenientFreightInvoiceTargetRequiredNameRule(var message: String) : LenientFreightInvoiceTargetRule {
    override fun check(item: LenientFreightInvoiceTarget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientFreightInvoiceTargetRequiredContactEmailRule(
    var message: String,
) : LenientFreightInvoiceTargetRule {
    override fun check(item: LenientFreightInvoiceTarget): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientFreightInvoiceTargetRequiredCodeRule(var message: String) : LenientFreightInvoiceTargetRule {
    override fun check(item: LenientFreightInvoiceTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientFreightInvoiceTargetDiscountRangeRule(
    var min: Double,
    var max: Double,
) : LenientFreightInvoiceTargetRule {
    override fun check(item: LenientFreightInvoiceTarget): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient freight invoice target. */
data class LenientFreightInvoiceTargetValidator(var rules: MutableList<LenientFreightInvoiceTargetRule>) {
    fun validate(item: LenientFreightInvoiceTarget): MutableList<String> {
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

    fun requireValid(item: LenientFreightInvoiceTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientFreightInvoiceTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientFreightInvoiceTargetValidator(): LenientFreightInvoiceTargetValidator {
    return LenientFreightInvoiceTargetValidator(
        mutableListOf<LenientFreightInvoiceTargetRule>(
            LenientFreightInvoiceTargetRequiredNameRule("lenient freight invoice target needs an entry"),
            LenientFreightInvoiceTargetRequiredContactEmailRule("lenient freight invoice target needs an entry"),
            LenientFreightInvoiceTargetRequiredCodeRule("lenient freight invoice target needs an entry"),
            LenientFreightInvoiceTargetDiscountRangeRule(8.1, 4343.6),
        ),
    )
}
