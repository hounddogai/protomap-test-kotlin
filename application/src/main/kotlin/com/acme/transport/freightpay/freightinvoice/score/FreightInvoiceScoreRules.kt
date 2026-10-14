package com.acme.transport.freightpay.freightinvoice.score

/** A rule a freight invoice score must pass. Check returns its problems, or none. */
interface FreightInvoiceScoreRule {
    fun check(item: FreightInvoiceScore): MutableList<String>
}

data class FreightInvoiceScoreReviewerLengthRule(var maxLength: Int) : FreightInvoiceScoreRule {
    override fun check(item: FreightInvoiceScore): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceScoreRequiredNameRule(var message: String) : FreightInvoiceScoreRule {
    override fun check(item: FreightInvoiceScore): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceScoreRequiredCodeRule(var message: String) : FreightInvoiceScoreRule {
    override fun check(item: FreightInvoiceScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice score. */
data class FreightInvoiceScoreValidator(var rules: MutableList<FreightInvoiceScoreRule>) {
    fun validate(item: FreightInvoiceScore): MutableList<String> {
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

    fun requireValid(item: FreightInvoiceScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceScore>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceScoreValidator(): FreightInvoiceScoreValidator {
    return FreightInvoiceScoreValidator(
        mutableListOf<FreightInvoiceScoreRule>(
            FreightInvoiceScoreReviewerLengthRule(32),
            FreightInvoiceScoreRequiredNameRule("freight invoice score needs an entry"),
            FreightInvoiceScoreRequiredCodeRule("freight invoice score needs a value"),
        ),
    )
}
