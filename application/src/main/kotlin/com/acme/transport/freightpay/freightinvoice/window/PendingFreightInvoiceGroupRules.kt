package com.acme.transport.freightpay.freightinvoice.window

/** A rule a pending freight invoice group must pass. Check returns its problems, or none. */
interface PendingFreightInvoiceGroupRule {
    fun check(item: PendingFreightInvoiceGroup): MutableList<String>
}

data class PendingFreightInvoiceGroupTitleLengthRule(var maxLength: Int) : PendingFreightInvoiceGroupRule {
    override fun check(item: PendingFreightInvoiceGroup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingFreightInvoiceGroupRequiredCodeRule(var message: String) : PendingFreightInvoiceGroupRule {
    override fun check(item: PendingFreightInvoiceGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingFreightInvoiceGroupDiscountRangeRule(
    var min: Double,
    var max: Double,
) : PendingFreightInvoiceGroupRule {
    override fun check(item: PendingFreightInvoiceGroup): MutableList<String> {
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

data class PendingFreightInvoiceGroupRequiredLabelRule(var message: String) : PendingFreightInvoiceGroupRule {
    override fun check(item: PendingFreightInvoiceGroup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingFreightInvoiceGroupCodeLengthRule(var maxLength: Int) : PendingFreightInvoiceGroupRule {
    override fun check(item: PendingFreightInvoiceGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending freight invoice group. */
data class PendingFreightInvoiceGroupValidator(var rules: MutableList<PendingFreightInvoiceGroupRule>) {
    fun validate(item: PendingFreightInvoiceGroup): MutableList<String> {
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

    fun requireValid(item: PendingFreightInvoiceGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingFreightInvoiceGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingFreightInvoiceGroupValidator(): PendingFreightInvoiceGroupValidator {
    return PendingFreightInvoiceGroupValidator(
        mutableListOf<PendingFreightInvoiceGroupRule>(
            PendingFreightInvoiceGroupTitleLengthRule(86400),
            PendingFreightInvoiceGroupRequiredCodeRule("pending freight invoice group needs an entry"),
            PendingFreightInvoiceGroupDiscountRangeRule(1.1, 4647.7),
            PendingFreightInvoiceGroupRequiredLabelRule("pending freight invoice group needs this field"),
            PendingFreightInvoiceGroupCodeLengthRule(32),
        ),
    )
}
