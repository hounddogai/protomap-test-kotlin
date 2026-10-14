package com.acme.transport.freightpay.freightinvoice.baseline

import com.acme.transport.kit.text.normalize

/** A rule a rolling freight invoice baseline must pass. Check returns its problems, or none. */
interface RollingFreightInvoiceBaselineRule {
    fun check(item: RollingFreightInvoiceBaseline): MutableList<String>
}

data class RollingFreightInvoiceBaselineAllowedTitleRule(
    var allowed: MutableList<String>,
) : RollingFreightInvoiceBaselineRule {
    override fun check(item: RollingFreightInvoiceBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is too long")
        }
        return mutableListOf<String>()
    }
}

data class RollingFreightInvoiceBaselineQuantityRangeRule(
    var min: Int,
    var max: Int,
) : RollingFreightInvoiceBaselineRule {
    override fun check(item: RollingFreightInvoiceBaseline): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingFreightInvoiceBaselineRequiredTimezoneRule(
    var message: String,
) : RollingFreightInvoiceBaselineRule {
    override fun check(item: RollingFreightInvoiceBaseline): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingFreightInvoiceBaselineShiftCodeLengthRule(
    var maxLength: Int,
) : RollingFreightInvoiceBaselineRule {
    override fun check(item: RollingFreightInvoiceBaseline): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingFreightInvoiceBaselineCodePrefixRule(var prefix: String) : RollingFreightInvoiceBaselineRule {
    override fun check(item: RollingFreightInvoiceBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling freight invoice baseline. */
data class RollingFreightInvoiceBaselineValidator(var rules: MutableList<RollingFreightInvoiceBaselineRule>) {
    fun validate(item: RollingFreightInvoiceBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingFreightInvoiceBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingFreightInvoiceBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingFreightInvoiceBaselineValidator(): RollingFreightInvoiceBaselineValidator {
    return RollingFreightInvoiceBaselineValidator(
        mutableListOf<RollingFreightInvoiceBaselineRule>(
            RollingFreightInvoiceBaselineAllowedTitleRule(mutableListOf<String>()),
            RollingFreightInvoiceBaselineQuantityRangeRule(16, 32),
            RollingFreightInvoiceBaselineRequiredTimezoneRule("rolling freight invoice baseline needs an entry"),
            RollingFreightInvoiceBaselineShiftCodeLengthRule(86400),
            RollingFreightInvoiceBaselineCodePrefixRule("ACM"),
        ),
    )
}
