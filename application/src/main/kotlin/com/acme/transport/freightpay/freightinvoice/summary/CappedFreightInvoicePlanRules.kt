package com.acme.transport.freightpay.freightinvoice.summary

import com.acme.transport.kit.text.normalize

/** A rule a capped freight invoice plan must pass. Check returns its problems, or none. */
interface CappedFreightInvoicePlanRule {
    fun check(item: CappedFreightInvoicePlan): MutableList<String>
}

data class CappedFreightInvoicePlanSegmentLengthRule(var maxLength: Int) : CappedFreightInvoicePlanRule {
    override fun check(item: CappedFreightInvoicePlan): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedFreightInvoicePlanCodePrefixRule(var prefix: String) : CappedFreightInvoicePlanRule {
    override fun check(item: CappedFreightInvoicePlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedFreightInvoicePlanAllowedVehicleIdRule(
    var allowed: MutableList<String>,
) : CappedFreightInvoicePlanRule {
    override fun check(item: CappedFreightInvoicePlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped freight invoice plan. */
data class CappedFreightInvoicePlanValidator(var rules: MutableList<CappedFreightInvoicePlanRule>) {
    fun validate(item: CappedFreightInvoicePlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedFreightInvoicePlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedFreightInvoicePlan>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedFreightInvoicePlanValidator(): CappedFreightInvoicePlanValidator {
    return CappedFreightInvoicePlanValidator(
        mutableListOf<CappedFreightInvoicePlanRule>(
            CappedFreightInvoicePlanSegmentLengthRule(3600),
            CappedFreightInvoicePlanCodePrefixRule("REF"),
            CappedFreightInvoicePlanAllowedVehicleIdRule(mutableListOf<String>()),
        ),
    )
}
