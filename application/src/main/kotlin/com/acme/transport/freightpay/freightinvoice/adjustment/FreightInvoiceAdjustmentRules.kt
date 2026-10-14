package com.acme.transport.freightpay.freightinvoice.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a freight invoice adjustment must pass. Check returns its problems, or none. */
interface FreightInvoiceAdjustmentRule {
    fun check(item: FreightInvoiceAdjustment): MutableList<String>
}

data class FreightInvoiceAdjustmentAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : FreightInvoiceAdjustmentRule {
    override fun check(item: FreightInvoiceAdjustment): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceAdjustmentCodeLengthRule(var maxLength: Int) : FreightInvoiceAdjustmentRule {
    override fun check(item: FreightInvoiceAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceAdjustmentRequiredCodeRule(var message: String) : FreightInvoiceAdjustmentRule {
    override fun check(item: FreightInvoiceAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceAdjustmentAllowedRegionRule(
    var allowed: MutableList<String>,
) : FreightInvoiceAdjustmentRule {
    override fun check(item: FreightInvoiceAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceAdjustmentRequiredCategoryRule(var message: String) : FreightInvoiceAdjustmentRule {
    override fun check(item: FreightInvoiceAdjustment): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceAdjustmentCategoryLengthRule(var maxLength: Int) : FreightInvoiceAdjustmentRule {
    override fun check(item: FreightInvoiceAdjustment): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice adjustment. */
data class FreightInvoiceAdjustmentValidator(var rules: MutableList<FreightInvoiceAdjustmentRule>) {
    fun validate(item: FreightInvoiceAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FreightInvoiceAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceAdjustmentValidator(): FreightInvoiceAdjustmentValidator {
    return FreightInvoiceAdjustmentValidator(
        mutableListOf<FreightInvoiceAdjustmentRule>(
            FreightInvoiceAdjustmentAttemptsRangeRule(16, 64),
            FreightInvoiceAdjustmentCodeLengthRule(86400),
            FreightInvoiceAdjustmentRequiredCodeRule("freight invoice adjustment needs this field"),
            FreightInvoiceAdjustmentAllowedRegionRule(mutableListOf<String>()),
            FreightInvoiceAdjustmentRequiredCategoryRule("freight invoice adjustment needs this field"),
            FreightInvoiceAdjustmentCategoryLengthRule(80),
        ),
    )
}
