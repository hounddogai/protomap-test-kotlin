package com.acme.transport.freightpay.freightinvoice.review

/** A rule a batched freight invoice review must pass. Check returns its problems, or none. */
interface BatchedFreightInvoiceReviewRule {
    fun check(item: BatchedFreightInvoiceReview): MutableList<String>
}

data class BatchedFreightInvoiceReviewRequiredTitleRule(var message: String) : BatchedFreightInvoiceReviewRule {
    override fun check(item: BatchedFreightInvoiceReview): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedFreightInvoiceReviewRequiredRouteCodeRule(
    var message: String,
) : BatchedFreightInvoiceReviewRule {
    override fun check(item: BatchedFreightInvoiceReview): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedFreightInvoiceReviewSourceLengthRule(var maxLength: Int) : BatchedFreightInvoiceReviewRule {
    override fun check(item: BatchedFreightInvoiceReview): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched freight invoice review. */
data class BatchedFreightInvoiceReviewValidator(var rules: MutableList<BatchedFreightInvoiceReviewRule>) {
    fun validate(item: BatchedFreightInvoiceReview): MutableList<String> {
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

    fun requireValid(item: BatchedFreightInvoiceReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedFreightInvoiceReview>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedFreightInvoiceReviewValidator(): BatchedFreightInvoiceReviewValidator {
    return BatchedFreightInvoiceReviewValidator(
        mutableListOf<BatchedFreightInvoiceReviewRule>(
            BatchedFreightInvoiceReviewRequiredTitleRule("batched freight invoice review needs this field"),
            BatchedFreightInvoiceReviewRequiredRouteCodeRule("batched freight invoice review needs an entry"),
            BatchedFreightInvoiceReviewSourceLengthRule(86400),
        ),
    )
}
