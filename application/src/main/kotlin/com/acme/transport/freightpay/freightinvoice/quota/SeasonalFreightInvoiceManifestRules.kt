package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.kit.text.normalize

/** A rule a seasonal freight invoice manifest must pass. Check returns its problems, or none. */
interface SeasonalFreightInvoiceManifestRule {
    fun check(item: SeasonalFreightInvoiceManifest): MutableList<String>
}

data class SeasonalFreightInvoiceManifestCountryLengthRule(
    var maxLength: Int,
) : SeasonalFreightInvoiceManifestRule {
    override fun check(item: SeasonalFreightInvoiceManifest): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFreightInvoiceManifestAllowedLabelRule(
    var allowed: MutableList<String>,
) : SeasonalFreightInvoiceManifestRule {
    override fun check(item: SeasonalFreightInvoiceManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFreightInvoiceManifestThresholdRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalFreightInvoiceManifestRule {
    override fun check(item: SeasonalFreightInvoiceManifest): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalFreightInvoiceManifestCodeLengthRule(
    var maxLength: Int,
) : SeasonalFreightInvoiceManifestRule {
    override fun check(item: SeasonalFreightInvoiceManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal freight invoice manifest. */
data class SeasonalFreightInvoiceManifestValidator(var rules: MutableList<SeasonalFreightInvoiceManifestRule>) {
    fun validate(item: SeasonalFreightInvoiceManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalFreightInvoiceManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalFreightInvoiceManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalFreightInvoiceManifestValidator(): SeasonalFreightInvoiceManifestValidator {
    return SeasonalFreightInvoiceManifestValidator(
        mutableListOf<SeasonalFreightInvoiceManifestRule>(
            SeasonalFreightInvoiceManifestCountryLengthRule(64),
            SeasonalFreightInvoiceManifestAllowedLabelRule(mutableListOf<String>("manual")),
            SeasonalFreightInvoiceManifestThresholdRangeRule(3.1, 2706.8),
            SeasonalFreightInvoiceManifestCodeLengthRule(80),
        ),
    )
}
