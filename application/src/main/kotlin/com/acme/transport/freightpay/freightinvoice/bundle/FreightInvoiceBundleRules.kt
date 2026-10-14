package com.acme.transport.freightpay.freightinvoice.bundle

import com.acme.transport.kit.text.normalize

/** A rule a freight invoice bundle must pass. Check returns its problems, or none. */
interface FreightInvoiceBundleRule {
    fun check(item: FreightInvoiceBundle): MutableList<String>
}

data class FreightInvoiceBundleRequiredTrailerIdRule(var message: String) : FreightInvoiceBundleRule {
    override fun check(item: FreightInvoiceBundle): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceBundleQuantityRangeRule(var min: Int, var max: Int) : FreightInvoiceBundleRule {
    override fun check(item: FreightInvoiceBundle): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceBundleAllowedShiftCodeRule(
    var allowed: MutableList<String>,
) : FreightInvoiceBundleRule {
    override fun check(item: FreightInvoiceBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceBundleStartsAtRangeRule(var min: Int, var max: Int) : FreightInvoiceBundleRule {
    override fun check(item: FreightInvoiceBundle): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceBundleCodePrefixRule(var prefix: String) : FreightInvoiceBundleRule {
    override fun check(item: FreightInvoiceBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceBundleNameLengthRule(var maxLength: Int) : FreightInvoiceBundleRule {
    override fun check(item: FreightInvoiceBundle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice bundle. */
data class FreightInvoiceBundleValidator(var rules: MutableList<FreightInvoiceBundleRule>) {
    fun validate(item: FreightInvoiceBundle): MutableList<String> {
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

    fun requireValid(item: FreightInvoiceBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceBundleValidator(): FreightInvoiceBundleValidator {
    return FreightInvoiceBundleValidator(
        mutableListOf<FreightInvoiceBundleRule>(
            FreightInvoiceBundleRequiredTrailerIdRule("freight invoice bundle needs an entry"),
            FreightInvoiceBundleQuantityRangeRule(16, 1000),
            FreightInvoiceBundleAllowedShiftCodeRule(mutableListOf<String>("adaptive")),
            FreightInvoiceBundleStartsAtRangeRule(16, 120),
            FreightInvoiceBundleCodePrefixRule("OPS"),
            FreightInvoiceBundleNameLengthRule(64),
        ),
    )
}
