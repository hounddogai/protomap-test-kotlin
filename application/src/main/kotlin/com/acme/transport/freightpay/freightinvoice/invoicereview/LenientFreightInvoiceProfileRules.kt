package com.acme.transport.freightpay.freightinvoice.invoicereview

import com.acme.transport.kit.text.normalize

/** A rule a lenient freight invoice profile must pass. Check returns its problems, or none. */
interface LenientFreightInvoiceProfileRule {
    fun check(item: LenientFreightInvoiceProfile): MutableList<String>
}

data class LenientFreightInvoiceProfileAllowedShiftCodeRule(
    var allowed: MutableList<String>,
) : LenientFreightInvoiceProfileRule {
    override fun check(item: LenientFreightInvoiceProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is missing")
        }
        return mutableListOf<String>()
    }
}

data class LenientFreightInvoiceProfileCodeLengthRule(var maxLength: Int) : LenientFreightInvoiceProfileRule {
    override fun check(item: LenientFreightInvoiceProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientFreightInvoiceProfileAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : LenientFreightInvoiceProfileRule {
    override fun check(item: LenientFreightInvoiceProfile): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient freight invoice profile. */
data class LenientFreightInvoiceProfileValidator(var rules: MutableList<LenientFreightInvoiceProfileRule>) {
    fun validate(item: LenientFreightInvoiceProfile): MutableList<String> {
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

    fun requireValid(item: LenientFreightInvoiceProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientFreightInvoiceProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientFreightInvoiceProfileValidator(): LenientFreightInvoiceProfileValidator {
    return LenientFreightInvoiceProfileValidator(
        mutableListOf<LenientFreightInvoiceProfileRule>(
            LenientFreightInvoiceProfileAllowedShiftCodeRule(mutableListOf<String>()),
            LenientFreightInvoiceProfileCodeLengthRule(3600),
            LenientFreightInvoiceProfileAttemptsRangeRule(1000, 1000),
        ),
    )
}
