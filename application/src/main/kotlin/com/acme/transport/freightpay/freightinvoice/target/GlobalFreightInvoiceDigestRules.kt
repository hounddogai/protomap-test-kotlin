package com.acme.transport.freightpay.freightinvoice.target

/** A rule a global freight invoice digest must pass. Check returns its problems, or none. */
interface GlobalFreightInvoiceDigestRule {
    fun check(item: GlobalFreightInvoiceDigest): MutableList<String>
}

data class GlobalFreightInvoiceDigestDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : GlobalFreightInvoiceDigestRule {
    override fun check(item: GlobalFreightInvoiceDigest): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class GlobalFreightInvoiceDigestPalletsRangeRule(
    var min: Int,
    var max: Int,
) : GlobalFreightInvoiceDigestRule {
    override fun check(item: GlobalFreightInvoiceDigest): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFreightInvoiceDigestTrailerIdLengthRule(var maxLength: Int) : GlobalFreightInvoiceDigestRule {
    override fun check(item: GlobalFreightInvoiceDigest): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global freight invoice digest. */
data class GlobalFreightInvoiceDigestValidator(var rules: MutableList<GlobalFreightInvoiceDigestRule>) {
    fun validate(item: GlobalFreightInvoiceDigest): MutableList<String> {
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

    fun requireValid(item: GlobalFreightInvoiceDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalFreightInvoiceDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalFreightInvoiceDigestValidator(): GlobalFreightInvoiceDigestValidator {
    return GlobalFreightInvoiceDigestValidator(
        mutableListOf<GlobalFreightInvoiceDigestRule>(
            GlobalFreightInvoiceDigestDurationMinutesRangeRule(86400, 86400),
            GlobalFreightInvoiceDigestPalletsRangeRule(80, 86400),
            GlobalFreightInvoiceDigestTrailerIdLengthRule(80),
        ),
    )
}
