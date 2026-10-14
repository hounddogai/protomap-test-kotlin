package com.acme.transport.freightpay.freightinvoice.template

/** A rule a primary freight invoice template must pass. Check returns its problems, or none. */
interface PrimaryFreightInvoiceTemplateRule {
    fun check(item: PrimaryFreightInvoiceTemplate): MutableList<String>
}

data class PrimaryFreightInvoiceTemplateNameLengthRule(var maxLength: Int) : PrimaryFreightInvoiceTemplateRule {
    override fun check(item: PrimaryFreightInvoiceTemplate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFreightInvoiceTemplateCodeLengthRule(var maxLength: Int) : PrimaryFreightInvoiceTemplateRule {
    override fun check(item: PrimaryFreightInvoiceTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFreightInvoiceTemplateFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryFreightInvoiceTemplateRule {
    override fun check(item: PrimaryFreightInvoiceTemplate): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a primary freight invoice template. */
data class PrimaryFreightInvoiceTemplateValidator(var rules: MutableList<PrimaryFreightInvoiceTemplateRule>) {
    fun validate(item: PrimaryFreightInvoiceTemplate): MutableList<String> {
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

    fun requireValid(item: PrimaryFreightInvoiceTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryFreightInvoiceTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryFreightInvoiceTemplateValidator(): PrimaryFreightInvoiceTemplateValidator {
    return PrimaryFreightInvoiceTemplateValidator(
        mutableListOf<PrimaryFreightInvoiceTemplateRule>(
            PrimaryFreightInvoiceTemplateNameLengthRule(86400),
            PrimaryFreightInvoiceTemplateCodeLengthRule(3600),
            PrimaryFreightInvoiceTemplateFloorValueRangeRule(1.5, 936.8),
        ),
    )
}
