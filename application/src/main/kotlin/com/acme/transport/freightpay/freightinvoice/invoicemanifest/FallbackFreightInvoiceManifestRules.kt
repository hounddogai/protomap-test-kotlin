package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import com.acme.transport.kit.text.normalize

/** A rule a fallback freight invoice manifest must pass. Check returns its problems, or none. */
interface FallbackFreightInvoiceManifestRule {
    fun check(item: FallbackFreightInvoiceManifest): MutableList<String>
}

data class FallbackFreightInvoiceManifestAllowedTitleRule(
    var allowed: MutableList<String>,
) : FallbackFreightInvoiceManifestRule {
    override fun check(item: FallbackFreightInvoiceManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is expired")
        }
        return mutableListOf<String>()
    }
}

data class FallbackFreightInvoiceManifestCodePrefixRule(
    var prefix: String,
) : FallbackFreightInvoiceManifestRule {
    override fun check(item: FallbackFreightInvoiceManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackFreightInvoiceManifestMarginRangeRule(
    var min: Double,
    var max: Double,
) : FallbackFreightInvoiceManifestRule {
    override fun check(item: FallbackFreightInvoiceManifest): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class FallbackFreightInvoiceManifestAmountRangeRule(
    var min: Double,
    var max: Double,
) : FallbackFreightInvoiceManifestRule {
    override fun check(item: FallbackFreightInvoiceManifest): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback freight invoice manifest. */
data class FallbackFreightInvoiceManifestValidator(var rules: MutableList<FallbackFreightInvoiceManifestRule>) {
    fun validate(item: FallbackFreightInvoiceManifest): MutableList<String> {
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

    fun requireValid(item: FallbackFreightInvoiceManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackFreightInvoiceManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackFreightInvoiceManifestValidator(): FallbackFreightInvoiceManifestValidator {
    return FallbackFreightInvoiceManifestValidator(
        mutableListOf<FallbackFreightInvoiceManifestRule>(
            FallbackFreightInvoiceManifestAllowedTitleRule(mutableListOf<String>()),
            FallbackFreightInvoiceManifestCodePrefixRule("OPS"),
            FallbackFreightInvoiceManifestMarginRangeRule(9.9, 595.7),
            FallbackFreightInvoiceManifestAmountRangeRule(0.6, 2211.8),
        ),
    )
}
