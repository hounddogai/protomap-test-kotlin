package com.acme.transport.freightpay.freightinvoice.preference

/** A rule a freight invoice batch must pass. Check returns its problems, or none. */
interface FreightInvoiceBatchRule {
    fun check(item: FreightInvoiceBatch): MutableList<String>
}

data class FreightInvoiceBatchRequiredOwnerIdRule(var message: String) : FreightInvoiceBatchRule {
    override fun check(item: FreightInvoiceBatch): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceBatchPhoneLengthRule(var maxLength: Int) : FreightInvoiceBatchRule {
    override fun check(item: FreightInvoiceBatch): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceBatchUpdatedAtRangeRule(var min: Int, var max: Int) : FreightInvoiceBatchRule {
    override fun check(item: FreightInvoiceBatch): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceBatchRequiredTitleRule(var message: String) : FreightInvoiceBatchRule {
    override fun check(item: FreightInvoiceBatch): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceBatchMinimumRangeRule(var min: Double, var max: Double) : FreightInvoiceBatchRule {
    override fun check(item: FreightInvoiceBatch): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice batch. */
data class FreightInvoiceBatchValidator(var rules: MutableList<FreightInvoiceBatchRule>) {
    fun validate(item: FreightInvoiceBatch): MutableList<String> {
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

    fun requireValid(item: FreightInvoiceBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceBatchValidator(): FreightInvoiceBatchValidator {
    return FreightInvoiceBatchValidator(
        mutableListOf<FreightInvoiceBatchRule>(
            FreightInvoiceBatchRequiredOwnerIdRule("freight invoice batch needs a value"),
            FreightInvoiceBatchPhoneLengthRule(80),
            FreightInvoiceBatchUpdatedAtRangeRule(80, 86400),
            FreightInvoiceBatchRequiredTitleRule("freight invoice batch needs this field"),
            FreightInvoiceBatchMinimumRangeRule(0.3, 2432.4),
        ),
    )
}
