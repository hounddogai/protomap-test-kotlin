package com.acme.transport.freightpay.freightinvoice.batch

/** A rule a default freight invoice batch must pass. Check returns its problems, or none. */
interface DefaultFreightInvoiceBatchRule {
    fun check(item: DefaultFreightInvoiceBatch): MutableList<String>
}

data class DefaultFreightInvoiceBatchNameLengthRule(var maxLength: Int) : DefaultFreightInvoiceBatchRule {
    override fun check(item: DefaultFreightInvoiceBatch): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultFreightInvoiceBatchRequiredNameRule(var message: String) : DefaultFreightInvoiceBatchRule {
    override fun check(item: DefaultFreightInvoiceBatch): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultFreightInvoiceBatchCodePrefixRule(var prefix: String) : DefaultFreightInvoiceBatchRule {
    override fun check(item: DefaultFreightInvoiceBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultFreightInvoiceBatchRouteCodeLengthRule(var maxLength: Int) : DefaultFreightInvoiceBatchRule {
    override fun check(item: DefaultFreightInvoiceBatch): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default freight invoice batch. */
data class DefaultFreightInvoiceBatchValidator(var rules: MutableList<DefaultFreightInvoiceBatchRule>) {
    fun validate(item: DefaultFreightInvoiceBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultFreightInvoiceBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultFreightInvoiceBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultFreightInvoiceBatchValidator(): DefaultFreightInvoiceBatchValidator {
    return DefaultFreightInvoiceBatchValidator(
        mutableListOf<DefaultFreightInvoiceBatchRule>(
            DefaultFreightInvoiceBatchNameLengthRule(255),
            DefaultFreightInvoiceBatchRequiredNameRule("default freight invoice batch needs a value"),
            DefaultFreightInvoiceBatchCodePrefixRule("OPS"),
            DefaultFreightInvoiceBatchRouteCodeLengthRule(64),
        ),
    )
}
