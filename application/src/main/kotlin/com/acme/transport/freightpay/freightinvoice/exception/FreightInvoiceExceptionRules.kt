package com.acme.transport.freightpay.freightinvoice.exception

/** A rule a freight invoice exception must pass. Check returns its problems, or none. */
interface FreightInvoiceExceptionRule {
    fun check(item: FreightInvoiceException): MutableList<String>
}

data class FreightInvoiceExceptionRequiredTitleRule(var message: String) : FreightInvoiceExceptionRule {
    override fun check(item: FreightInvoiceException): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceExceptionRequiredLaneRule(var message: String) : FreightInvoiceExceptionRule {
    override fun check(item: FreightInvoiceException): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceExceptionCeilingRangeRule(
    var min: Double,
    var max: Double,
) : FreightInvoiceExceptionRule {
    override fun check(item: FreightInvoiceException): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a freight invoice exception. */
data class FreightInvoiceExceptionValidator(var rules: MutableList<FreightInvoiceExceptionRule>) {
    fun validate(item: FreightInvoiceException): MutableList<String> {
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

    fun requireValid(item: FreightInvoiceException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceException>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceExceptionValidator(): FreightInvoiceExceptionValidator {
    return FreightInvoiceExceptionValidator(
        mutableListOf<FreightInvoiceExceptionRule>(
            FreightInvoiceExceptionRequiredTitleRule("freight invoice exception needs a value"),
            FreightInvoiceExceptionRequiredLaneRule("freight invoice exception needs an entry"),
            FreightInvoiceExceptionCeilingRangeRule(6.3, 3317.8),
        ),
    )
}
