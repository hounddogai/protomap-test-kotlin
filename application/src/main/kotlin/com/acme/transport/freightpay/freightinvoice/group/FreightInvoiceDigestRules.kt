package com.acme.transport.freightpay.freightinvoice.group

/** A rule a freight invoice digest must pass. Check returns its problems, or none. */
interface FreightInvoiceDigestRule {
    fun check(item: FreightInvoiceDigest): MutableList<String>
}

data class FreightInvoiceDigestRequiredLabelRule(var message: String) : FreightInvoiceDigestRule {
    override fun check(item: FreightInvoiceDigest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceDigestLabelLengthRule(var maxLength: Int) : FreightInvoiceDigestRule {
    override fun check(item: FreightInvoiceDigest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceDigestRouteCodeLengthRule(var maxLength: Int) : FreightInvoiceDigestRule {
    override fun check(item: FreightInvoiceDigest): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceDigestNameLengthRule(var maxLength: Int) : FreightInvoiceDigestRule {
    override fun check(item: FreightInvoiceDigest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceDigestRequiredNameRule(var message: String) : FreightInvoiceDigestRule {
    override fun check(item: FreightInvoiceDigest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceDigestCodeLengthRule(var maxLength: Int) : FreightInvoiceDigestRule {
    override fun check(item: FreightInvoiceDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice digest. */
data class FreightInvoiceDigestValidator(var rules: MutableList<FreightInvoiceDigestRule>) {
    fun validate(item: FreightInvoiceDigest): MutableList<String> {
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

    fun requireValid(item: FreightInvoiceDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceDigestValidator(): FreightInvoiceDigestValidator {
    return FreightInvoiceDigestValidator(
        mutableListOf<FreightInvoiceDigestRule>(
            FreightInvoiceDigestRequiredLabelRule("freight invoice digest needs a value"),
            FreightInvoiceDigestLabelLengthRule(120),
            FreightInvoiceDigestRouteCodeLengthRule(120),
            FreightInvoiceDigestNameLengthRule(255),
            FreightInvoiceDigestRequiredNameRule("freight invoice digest needs this field"),
            FreightInvoiceDigestCodeLengthRule(64),
        ),
    )
}
