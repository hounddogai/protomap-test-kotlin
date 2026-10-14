package com.acme.transport.freightpay.freightinvoice.notice

/** A rule a fallback freight invoice allocation must pass. Check returns its problems, or none. */
interface FallbackFreightInvoiceAllocationRule {
    fun check(item: FallbackFreightInvoiceAllocation): MutableList<String>
}

data class FallbackFreightInvoiceAllocationNameLengthRule(
    var maxLength: Int,
) : FallbackFreightInvoiceAllocationRule {
    override fun check(item: FallbackFreightInvoiceAllocation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackFreightInvoiceAllocationRequiredContactEmailRule(
    var message: String,
) : FallbackFreightInvoiceAllocationRule {
    override fun check(item: FallbackFreightInvoiceAllocation): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackFreightInvoiceAllocationDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : FallbackFreightInvoiceAllocationRule {
    override fun check(item: FallbackFreightInvoiceAllocation): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback freight invoice allocation. */
data class FallbackFreightInvoiceAllocationValidator(
    var rules: MutableList<FallbackFreightInvoiceAllocationRule>,
) {
    fun validate(item: FallbackFreightInvoiceAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackFreightInvoiceAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackFreightInvoiceAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackFreightInvoiceAllocationValidator(): FallbackFreightInvoiceAllocationValidator {
    return FallbackFreightInvoiceAllocationValidator(
        mutableListOf<FallbackFreightInvoiceAllocationRule>(
            FallbackFreightInvoiceAllocationNameLengthRule(86400),
            FallbackFreightInvoiceAllocationRequiredContactEmailRule(
                "fallback freight invoice allocation needs an entry",
            ),
            FallbackFreightInvoiceAllocationDistanceKmRangeRule(4.8, 3887.5),
        ),
    )
}
