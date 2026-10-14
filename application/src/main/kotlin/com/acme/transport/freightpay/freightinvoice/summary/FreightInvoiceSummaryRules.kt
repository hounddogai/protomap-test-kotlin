package com.acme.transport.freightpay.freightinvoice.summary

/** A rule a freight invoice summary must pass. Check returns its problems, or none. */
interface FreightInvoiceSummaryRule {
    fun check(item: FreightInvoiceSummary): MutableList<String>
}

data class FreightInvoiceSummaryBatchSizeRangeRule(var min: Int, var max: Int) : FreightInvoiceSummaryRule {
    override fun check(item: FreightInvoiceSummary): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class FreightInvoiceSummaryCodeLengthRule(var maxLength: Int) : FreightInvoiceSummaryRule {
    override fun check(item: FreightInvoiceSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceSummaryRequiredTitleRule(var message: String) : FreightInvoiceSummaryRule {
    override fun check(item: FreightInvoiceSummary): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice summary. */
data class FreightInvoiceSummaryValidator(var rules: MutableList<FreightInvoiceSummaryRule>) {
    fun validate(item: FreightInvoiceSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FreightInvoiceSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceSummaryValidator(): FreightInvoiceSummaryValidator {
    return FreightInvoiceSummaryValidator(
        mutableListOf<FreightInvoiceSummaryRule>(
            FreightInvoiceSummaryBatchSizeRangeRule(16, 3600),
            FreightInvoiceSummaryCodeLengthRule(255),
            FreightInvoiceSummaryRequiredTitleRule("freight invoice summary needs an entry"),
        ),
    )
}
