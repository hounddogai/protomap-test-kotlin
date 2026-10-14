package com.acme.transport.freightpay.freightinvoice.proposal

import com.acme.transport.kit.text.normalize

/** A rule a strict freight invoice request must pass. Check returns its problems, or none. */
interface StrictFreightInvoiceRequestRule {
    fun check(item: StrictFreightInvoiceRequest): MutableList<String>
}

data class StrictFreightInvoiceRequestNameLengthRule(var maxLength: Int) : StrictFreightInvoiceRequestRule {
    override fun check(item: StrictFreightInvoiceRequest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictFreightInvoiceRequestRequiredNameRule(var message: String) : StrictFreightInvoiceRequestRule {
    override fun check(item: StrictFreightInvoiceRequest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictFreightInvoiceRequestAllowedUnitRule(
    var allowed: MutableList<String>,
) : StrictFreightInvoiceRequestRule {
    override fun check(item: StrictFreightInvoiceRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is stale")
        }
        return mutableListOf<String>()
    }
}

data class StrictFreightInvoiceRequestUnitLengthRule(var maxLength: Int) : StrictFreightInvoiceRequestRule {
    override fun check(item: StrictFreightInvoiceRequest): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictFreightInvoiceRequestRequiredUnitRule(var message: String) : StrictFreightInvoiceRequestRule {
    override fun check(item: StrictFreightInvoiceRequest): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict freight invoice request. */
data class StrictFreightInvoiceRequestValidator(var rules: MutableList<StrictFreightInvoiceRequestRule>) {
    fun validate(item: StrictFreightInvoiceRequest): MutableList<String> {
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

    fun requireValid(item: StrictFreightInvoiceRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictFreightInvoiceRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictFreightInvoiceRequestValidator(): StrictFreightInvoiceRequestValidator {
    return StrictFreightInvoiceRequestValidator(
        mutableListOf<StrictFreightInvoiceRequestRule>(
            StrictFreightInvoiceRequestNameLengthRule(80),
            StrictFreightInvoiceRequestRequiredNameRule("strict freight invoice request needs an entry"),
            StrictFreightInvoiceRequestAllowedUnitRule(mutableListOf<String>()),
            StrictFreightInvoiceRequestUnitLengthRule(120),
            StrictFreightInvoiceRequestRequiredUnitRule("strict freight invoice request needs a value"),
        ),
    )
}
