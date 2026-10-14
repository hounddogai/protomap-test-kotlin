package com.acme.transport.freightpay.freightinvoice.window

/** A rule a rolling freight invoice window must pass. Check returns its problems, or none. */
interface RollingFreightInvoiceWindowRule {
    fun check(item: RollingFreightInvoiceWindow): MutableList<String>
}

data class RollingFreightInvoiceWindowLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : RollingFreightInvoiceWindowRule {
    override fun check(item: RollingFreightInvoiceWindow): MutableList<String> {
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

data class RollingFreightInvoiceWindowMaximumRangeRule(
    var min: Double,
    var max: Double,
) : RollingFreightInvoiceWindowRule {
    override fun check(item: RollingFreightInvoiceWindow): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingFreightInvoiceWindowCodePrefixRule(var prefix: String) : RollingFreightInvoiceWindowRule {
    override fun check(item: RollingFreightInvoiceWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingFreightInvoiceWindowCodeLengthRule(var maxLength: Int) : RollingFreightInvoiceWindowRule {
    override fun check(item: RollingFreightInvoiceWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling freight invoice window. */
data class RollingFreightInvoiceWindowValidator(var rules: MutableList<RollingFreightInvoiceWindowRule>) {
    fun validate(item: RollingFreightInvoiceWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingFreightInvoiceWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingFreightInvoiceWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingFreightInvoiceWindowValidator(): RollingFreightInvoiceWindowValidator {
    return RollingFreightInvoiceWindowValidator(
        mutableListOf<RollingFreightInvoiceWindowRule>(
            RollingFreightInvoiceWindowLoadKgRangeRule(3.5, 492.1),
            RollingFreightInvoiceWindowMaximumRangeRule(6.9, 195.5),
            RollingFreightInvoiceWindowCodePrefixRule("Q"),
            RollingFreightInvoiceWindowCodeLengthRule(80),
        ),
    )
}
