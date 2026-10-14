package com.acme.transport.freightpay.freightinvoice.entry

/** A rule a lenient freight invoice rollup must pass. Check returns its problems, or none. */
interface LenientFreightInvoiceRollupRule {
    fun check(item: LenientFreightInvoiceRollup): MutableList<String>
}

data class LenientFreightInvoiceRollupCodePrefixRule(var prefix: String) : LenientFreightInvoiceRollupRule {
    override fun check(item: LenientFreightInvoiceRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientFreightInvoiceRollupLabelLengthRule(var maxLength: Int) : LenientFreightInvoiceRollupRule {
    override fun check(item: LenientFreightInvoiceRollup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientFreightInvoiceRollupShiftCodeLengthRule(
    var maxLength: Int,
) : LenientFreightInvoiceRollupRule {
    override fun check(item: LenientFreightInvoiceRollup): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient freight invoice rollup. */
data class LenientFreightInvoiceRollupValidator(var rules: MutableList<LenientFreightInvoiceRollupRule>) {
    fun validate(item: LenientFreightInvoiceRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientFreightInvoiceRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientFreightInvoiceRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientFreightInvoiceRollupValidator(): LenientFreightInvoiceRollupValidator {
    return LenientFreightInvoiceRollupValidator(
        mutableListOf<LenientFreightInvoiceRollupRule>(
            LenientFreightInvoiceRollupCodePrefixRule("X"),
            LenientFreightInvoiceRollupLabelLengthRule(3600),
            LenientFreightInvoiceRollupShiftCodeLengthRule(80),
        ),
    )
}
