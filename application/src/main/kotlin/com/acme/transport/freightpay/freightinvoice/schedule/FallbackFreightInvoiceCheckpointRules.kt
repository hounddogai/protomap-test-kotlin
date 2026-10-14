package com.acme.transport.freightpay.freightinvoice.schedule

/** A rule a fallback freight invoice checkpoint must pass. Check returns its problems, or none. */
interface FallbackFreightInvoiceCheckpointRule {
    fun check(item: FallbackFreightInvoiceCheckpoint): MutableList<String>
}

data class FallbackFreightInvoiceCheckpointCodeLengthRule(
    var maxLength: Int,
) : FallbackFreightInvoiceCheckpointRule {
    override fun check(item: FallbackFreightInvoiceCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackFreightInvoiceCheckpointReviewerLengthRule(
    var maxLength: Int,
) : FallbackFreightInvoiceCheckpointRule {
    override fun check(item: FallbackFreightInvoiceCheckpoint): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackFreightInvoiceCheckpointRequiredContactEmailRule(
    var message: String,
) : FallbackFreightInvoiceCheckpointRule {
    override fun check(item: FallbackFreightInvoiceCheckpoint): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackFreightInvoiceCheckpointContactEmailLengthRule(
    var maxLength: Int,
) : FallbackFreightInvoiceCheckpointRule {
    override fun check(item: FallbackFreightInvoiceCheckpoint): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback freight invoice checkpoint. */
data class FallbackFreightInvoiceCheckpointValidator(
    var rules: MutableList<FallbackFreightInvoiceCheckpointRule>,
) {
    fun validate(item: FallbackFreightInvoiceCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackFreightInvoiceCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackFreightInvoiceCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackFreightInvoiceCheckpointValidator(): FallbackFreightInvoiceCheckpointValidator {
    return FallbackFreightInvoiceCheckpointValidator(
        mutableListOf<FallbackFreightInvoiceCheckpointRule>(
            FallbackFreightInvoiceCheckpointCodeLengthRule(32),
            FallbackFreightInvoiceCheckpointReviewerLengthRule(255),
            FallbackFreightInvoiceCheckpointRequiredContactEmailRule(
                "fallback freight invoice checkpoint needs this field",
            ),
            FallbackFreightInvoiceCheckpointContactEmailLengthRule(120),
        ),
    )
}
