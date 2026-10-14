package com.acme.transport.freightpay.freightinvoice.invoicereview

import com.acme.transport.kit.text.normalize

/** A rule a manual freight invoice review must pass. Check returns its problems, or none. */
interface ManualFreightInvoiceReviewRule {
    fun check(item: ManualFreightInvoiceReview): MutableList<String>
}

data class ManualFreightInvoiceReviewRequiredCodeRule(var message: String) : ManualFreightInvoiceReviewRule {
    override fun check(item: ManualFreightInvoiceReview): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualFreightInvoiceReviewAllowedCodeRule(
    var allowed: MutableList<String>,
) : ManualFreightInvoiceReviewRule {
    override fun check(item: ManualFreightInvoiceReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class ManualFreightInvoiceReviewAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : ManualFreightInvoiceReviewRule {
    override fun check(item: ManualFreightInvoiceReview): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual freight invoice review. */
data class ManualFreightInvoiceReviewValidator(var rules: MutableList<ManualFreightInvoiceReviewRule>) {
    fun validate(item: ManualFreightInvoiceReview): MutableList<String> {
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

    fun requireValid(item: ManualFreightInvoiceReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualFreightInvoiceReview>): MutableMap<String, MutableList<String>> {
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

fun defaultManualFreightInvoiceReviewValidator(): ManualFreightInvoiceReviewValidator {
    return ManualFreightInvoiceReviewValidator(
        mutableListOf<ManualFreightInvoiceReviewRule>(
            ManualFreightInvoiceReviewRequiredCodeRule("manual freight invoice review needs this field"),
            ManualFreightInvoiceReviewAllowedCodeRule(mutableListOf<String>("lenient")),
            ManualFreightInvoiceReviewAttemptsRangeRule(64, 120),
        ),
    )
}
