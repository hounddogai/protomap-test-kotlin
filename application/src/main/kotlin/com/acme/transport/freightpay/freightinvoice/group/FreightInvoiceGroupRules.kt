package com.acme.transport.freightpay.freightinvoice.group

/** A rule a freight invoice group must pass. Check returns its problems, or none. */
interface FreightInvoiceGroupRule {
    fun check(item: FreightInvoiceGroup): MutableList<String>
}

data class FreightInvoiceGroupCodePrefixRule(var prefix: String) : FreightInvoiceGroupRule {
    override fun check(item: FreightInvoiceGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceGroupRequiredCodeRule(var message: String) : FreightInvoiceGroupRule {
    override fun check(item: FreightInvoiceGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceGroupCodeLengthRule(var maxLength: Int) : FreightInvoiceGroupRule {
    override fun check(item: FreightInvoiceGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice group. */
data class FreightInvoiceGroupValidator(var rules: MutableList<FreightInvoiceGroupRule>) {
    fun validate(item: FreightInvoiceGroup): MutableList<String> {
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

    fun requireValid(item: FreightInvoiceGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceGroupValidator(): FreightInvoiceGroupValidator {
    return FreightInvoiceGroupValidator(
        mutableListOf<FreightInvoiceGroupRule>(
            FreightInvoiceGroupCodePrefixRule("Q"),
            FreightInvoiceGroupRequiredCodeRule("freight invoice group needs a value"),
            FreightInvoiceGroupCodeLengthRule(3600),
        ),
    )
}
