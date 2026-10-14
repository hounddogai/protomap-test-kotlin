package com.acme.transport.freightpay.freightinvoice.entry

/** A rule a pending freight invoice rollup must pass. Check returns its problems, or none. */
interface PendingFreightInvoiceRollupRule {
    fun check(item: PendingFreightInvoiceRollup): MutableList<String>
}

data class PendingFreightInvoiceRollupOdometerRangeRule(
    var min: Int,
    var max: Int,
) : PendingFreightInvoiceRollupRule {
    override fun check(item: PendingFreightInvoiceRollup): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class PendingFreightInvoiceRollupFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : PendingFreightInvoiceRollupRule {
    override fun check(item: PendingFreightInvoiceRollup): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class PendingFreightInvoiceRollupCodeLengthRule(var maxLength: Int) : PendingFreightInvoiceRollupRule {
    override fun check(item: PendingFreightInvoiceRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingFreightInvoiceRollupLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : PendingFreightInvoiceRollupRule {
    override fun check(item: PendingFreightInvoiceRollup): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a pending freight invoice rollup. */
data class PendingFreightInvoiceRollupValidator(var rules: MutableList<PendingFreightInvoiceRollupRule>) {
    fun validate(item: PendingFreightInvoiceRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingFreightInvoiceRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingFreightInvoiceRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingFreightInvoiceRollupValidator(): PendingFreightInvoiceRollupValidator {
    return PendingFreightInvoiceRollupValidator(
        mutableListOf<PendingFreightInvoiceRollupRule>(
            PendingFreightInvoiceRollupOdometerRangeRule(64, 120),
            PendingFreightInvoiceRollupFuelLitersRangeRule(8.9, 3098.9),
            PendingFreightInvoiceRollupCodeLengthRule(32),
            PendingFreightInvoiceRollupLoadKgRangeRule(7.1, 2888.0),
        ),
    )
}
