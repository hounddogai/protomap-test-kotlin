package com.acme.transport.freightpay.freightinvoice.invoicerollup

/** A rule a global freight invoice rollup must pass. Check returns its problems, or none. */
interface GlobalFreightInvoiceRollupRule {
    fun check(item: GlobalFreightInvoiceRollup): MutableList<String>
}

data class GlobalFreightInvoiceRollupRequiredNameRule(var message: String) : GlobalFreightInvoiceRollupRule {
    override fun check(item: GlobalFreightInvoiceRollup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalFreightInvoiceRollupScoreRangeRule(
    var min: Double,
    var max: Double,
) : GlobalFreightInvoiceRollupRule {
    override fun check(item: GlobalFreightInvoiceRollup): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFreightInvoiceRollupCeilingRangeRule(
    var min: Double,
    var max: Double,
) : GlobalFreightInvoiceRollupRule {
    override fun check(item: GlobalFreightInvoiceRollup): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class GlobalFreightInvoiceRollupRequiredTitleRule(var message: String) : GlobalFreightInvoiceRollupRule {
    override fun check(item: GlobalFreightInvoiceRollup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalFreightInvoiceRollupCodeLengthRule(var maxLength: Int) : GlobalFreightInvoiceRollupRule {
    override fun check(item: GlobalFreightInvoiceRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFreightInvoiceRollupRequiredTrailerIdRule(
    var message: String,
) : GlobalFreightInvoiceRollupRule {
    override fun check(item: GlobalFreightInvoiceRollup): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global freight invoice rollup. */
data class GlobalFreightInvoiceRollupValidator(var rules: MutableList<GlobalFreightInvoiceRollupRule>) {
    fun validate(item: GlobalFreightInvoiceRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalFreightInvoiceRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalFreightInvoiceRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalFreightInvoiceRollupValidator(): GlobalFreightInvoiceRollupValidator {
    return GlobalFreightInvoiceRollupValidator(
        mutableListOf<GlobalFreightInvoiceRollupRule>(
            GlobalFreightInvoiceRollupRequiredNameRule("global freight invoice rollup needs an entry"),
            GlobalFreightInvoiceRollupScoreRangeRule(5.1, 3242.4),
            GlobalFreightInvoiceRollupCeilingRangeRule(7.7, 4893.2),
            GlobalFreightInvoiceRollupRequiredTitleRule("global freight invoice rollup needs a value"),
            GlobalFreightInvoiceRollupCodeLengthRule(80),
            GlobalFreightInvoiceRollupRequiredTrailerIdRule("global freight invoice rollup needs a value"),
        ),
    )
}
