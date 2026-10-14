package com.acme.transport.freightpay.freightinvoice.allocation

/** A rule a freight invoice allocation must pass. Check returns its problems, or none. */
interface FreightInvoiceAllocationRule {
    fun check(item: FreightInvoiceAllocation): MutableList<String>
}

data class FreightInvoiceAllocationCodePrefixRule(var prefix: String) : FreightInvoiceAllocationRule {
    override fun check(item: FreightInvoiceAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceAllocationRequiredDepotRule(var message: String) : FreightInvoiceAllocationRule {
    override fun check(item: FreightInvoiceAllocation): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceAllocationContactEmailLengthRule(var maxLength: Int) : FreightInvoiceAllocationRule {
    override fun check(item: FreightInvoiceAllocation): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceAllocationRequiredContactEmailRule(
    var message: String,
) : FreightInvoiceAllocationRule {
    override fun check(item: FreightInvoiceAllocation): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceAllocationRateRangeRule(
    var min: Double,
    var max: Double,
) : FreightInvoiceAllocationRule {
    override fun check(item: FreightInvoiceAllocation): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a freight invoice allocation. */
data class FreightInvoiceAllocationValidator(var rules: MutableList<FreightInvoiceAllocationRule>) {
    fun validate(item: FreightInvoiceAllocation): MutableList<String> {
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

    fun requireValid(item: FreightInvoiceAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceAllocationValidator(): FreightInvoiceAllocationValidator {
    return FreightInvoiceAllocationValidator(
        mutableListOf<FreightInvoiceAllocationRule>(
            FreightInvoiceAllocationCodePrefixRule("Q"),
            FreightInvoiceAllocationRequiredDepotRule("freight invoice allocation needs a value"),
            FreightInvoiceAllocationContactEmailLengthRule(64),
            FreightInvoiceAllocationRequiredContactEmailRule("freight invoice allocation needs a value"),
            FreightInvoiceAllocationRateRangeRule(2.8, 829.4),
        ),
    )
}
