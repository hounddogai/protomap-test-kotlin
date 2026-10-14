package com.acme.transport.freightpay.freightinvoice.manifest

import com.acme.transport.kit.text.normalize

/** A rule a freight invoice manifest must pass. Check returns its problems, or none. */
interface FreightInvoiceManifestRule {
    fun check(item: FreightInvoiceManifest): MutableList<String>
}

data class FreightInvoiceManifestExternalRefLengthRule(var maxLength: Int) : FreightInvoiceManifestRule {
    override fun check(item: FreightInvoiceManifest): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceManifestEndsAtRangeRule(var min: Int, var max: Int) : FreightInvoiceManifestRule {
    override fun check(item: FreightInvoiceManifest): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceManifestWindowRule(var maxSpan: Int) : FreightInvoiceManifestRule {
    override fun check(item: FreightInvoiceManifest): MutableList<String> {
        if (item.startsAt > item.endsAt) {
            return mutableListOf<String>("starts at is after ends at")
        }
        if (maxSpan > 0 && item.endsAt - item.startsAt > maxSpan) {
            return mutableListOf<String>("spans more than $maxSpan seconds")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceManifestStartsAtRangeRule(var min: Int, var max: Int) : FreightInvoiceManifestRule {
    override fun check(item: FreightInvoiceManifest): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class FreightInvoiceManifestWindowDaysRangeRule(var min: Int, var max: Int) : FreightInvoiceManifestRule {
    override fun check(item: FreightInvoiceManifest): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceManifestAllowedCodeRule(
    var allowed: MutableList<String>,
) : FreightInvoiceManifestRule {
    override fun check(item: FreightInvoiceManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice manifest. */
data class FreightInvoiceManifestValidator(var rules: MutableList<FreightInvoiceManifestRule>) {
    fun validate(item: FreightInvoiceManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FreightInvoiceManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceManifestValidator(): FreightInvoiceManifestValidator {
    return FreightInvoiceManifestValidator(
        mutableListOf<FreightInvoiceManifestRule>(
            FreightInvoiceManifestExternalRefLengthRule(80),
            FreightInvoiceManifestEndsAtRangeRule(64, 3600),
            FreightInvoiceManifestWindowRule(80),
            FreightInvoiceManifestStartsAtRangeRule(255, 255),
            FreightInvoiceManifestWindowDaysRangeRule(16, 3600),
            FreightInvoiceManifestAllowedCodeRule(mutableListOf<String>("scheduled", "lenient")),
        ),
    )
}
