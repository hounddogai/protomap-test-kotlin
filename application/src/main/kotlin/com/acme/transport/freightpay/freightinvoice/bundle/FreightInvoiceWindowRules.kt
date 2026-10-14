package com.acme.transport.freightpay.freightinvoice.bundle

/** A rule a freight invoice window must pass. Check returns its problems, or none. */
interface FreightInvoiceWindowRule {
    fun check(item: FreightInvoiceWindow): MutableList<String>
}

data class FreightInvoiceWindowRequiredCodeRule(var message: String) : FreightInvoiceWindowRule {
    override fun check(item: FreightInvoiceWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceWindowMarginRangeRule(var min: Double, var max: Double) : FreightInvoiceWindowRule {
    override fun check(item: FreightInvoiceWindow): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceWindowAmountRangeRule(var min: Double, var max: Double) : FreightInvoiceWindowRule {
    override fun check(item: FreightInvoiceWindow): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice window. */
data class FreightInvoiceWindowValidator(var rules: MutableList<FreightInvoiceWindowRule>) {
    fun validate(item: FreightInvoiceWindow): MutableList<String> {
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

    fun requireValid(item: FreightInvoiceWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceWindowValidator(): FreightInvoiceWindowValidator {
    return FreightInvoiceWindowValidator(
        mutableListOf<FreightInvoiceWindowRule>(
            FreightInvoiceWindowRequiredCodeRule("freight invoice window needs a value"),
            FreightInvoiceWindowMarginRangeRule(8.3, 2874.1),
            FreightInvoiceWindowAmountRangeRule(3.1, 3635.9),
        ),
    )
}
