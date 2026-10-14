package com.acme.transport.freightpay.freightinvoice.digest

import com.acme.transport.kit.text.normalize

/** A rule a legacy freight invoice digest must pass. Check returns its problems, or none. */
interface LegacyFreightInvoiceDigestRule {
    fun check(item: LegacyFreightInvoiceDigest): MutableList<String>
}

data class LegacyFreightInvoiceDigestRatioRangeRule(
    var min: Double,
    var max: Double,
) : LegacyFreightInvoiceDigestRule {
    override fun check(item: LegacyFreightInvoiceDigest): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyFreightInvoiceDigestCapacityRangeRule(
    var min: Int,
    var max: Int,
) : LegacyFreightInvoiceDigestRule {
    override fun check(item: LegacyFreightInvoiceDigest): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class LegacyFreightInvoiceDigestAllowedCodeRule(
    var allowed: MutableList<String>,
) : LegacyFreightInvoiceDigestRule {
    override fun check(item: LegacyFreightInvoiceDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class LegacyFreightInvoiceDigestRequiredCodeRule(var message: String) : LegacyFreightInvoiceDigestRule {
    override fun check(item: LegacyFreightInvoiceDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyFreightInvoiceDigestCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : LegacyFreightInvoiceDigestRule {
    override fun check(item: LegacyFreightInvoiceDigest): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy freight invoice digest. */
data class LegacyFreightInvoiceDigestValidator(var rules: MutableList<LegacyFreightInvoiceDigestRule>) {
    fun validate(item: LegacyFreightInvoiceDigest): MutableList<String> {
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

    fun requireValid(item: LegacyFreightInvoiceDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyFreightInvoiceDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyFreightInvoiceDigestValidator(): LegacyFreightInvoiceDigestValidator {
    return LegacyFreightInvoiceDigestValidator(
        mutableListOf<LegacyFreightInvoiceDigestRule>(
            LegacyFreightInvoiceDigestRatioRangeRule(10.0, 275.3),
            LegacyFreightInvoiceDigestCapacityRangeRule(1000, 86400),
            LegacyFreightInvoiceDigestAllowedCodeRule(mutableListOf<String>("primary")),
            LegacyFreightInvoiceDigestRequiredCodeRule("legacy freight invoice digest needs an entry"),
            LegacyFreightInvoiceDigestCreatedAtRangeRule(80, 120),
        ),
    )
}
