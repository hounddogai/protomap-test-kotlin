package com.acme.transport.freightpay.freightinvoice.exception

/** A rule a manual freight invoice window must pass. Check returns its problems, or none. */
interface ManualFreightInvoiceWindowRule {
    fun check(item: ManualFreightInvoiceWindow): MutableList<String>
}

data class ManualFreightInvoiceWindowBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : ManualFreightInvoiceWindowRule {
    override fun check(item: ManualFreightInvoiceWindow): MutableList<String> {
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

data class ManualFreightInvoiceWindowCodePrefixRule(var prefix: String) : ManualFreightInvoiceWindowRule {
    override fun check(item: ManualFreightInvoiceWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualFreightInvoiceWindowTimezoneLengthRule(var maxLength: Int) : ManualFreightInvoiceWindowRule {
    override fun check(item: ManualFreightInvoiceWindow): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualFreightInvoiceWindowCodeLengthRule(var maxLength: Int) : ManualFreightInvoiceWindowRule {
    override fun check(item: ManualFreightInvoiceWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualFreightInvoiceWindowPalletsRangeRule(
    var min: Int,
    var max: Int,
) : ManualFreightInvoiceWindowRule {
    override fun check(item: ManualFreightInvoiceWindow): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualFreightInvoiceWindowEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : ManualFreightInvoiceWindowRule {
    override fun check(item: ManualFreightInvoiceWindow): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual freight invoice window. */
data class ManualFreightInvoiceWindowValidator(var rules: MutableList<ManualFreightInvoiceWindowRule>) {
    fun validate(item: ManualFreightInvoiceWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualFreightInvoiceWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualFreightInvoiceWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultManualFreightInvoiceWindowValidator(): ManualFreightInvoiceWindowValidator {
    return ManualFreightInvoiceWindowValidator(
        mutableListOf<ManualFreightInvoiceWindowRule>(
            ManualFreightInvoiceWindowBatchSizeRangeRule(16, 1000),
            ManualFreightInvoiceWindowCodePrefixRule("REF"),
            ManualFreightInvoiceWindowTimezoneLengthRule(64),
            ManualFreightInvoiceWindowCodeLengthRule(80),
            ManualFreightInvoiceWindowPalletsRangeRule(64, 80),
            ManualFreightInvoiceWindowEndsAtRangeRule(64, 86400),
        ),
    )
}
