package com.acme.transport.freightpay.freightinvoice.overrideitem

/** A rule a adaptive freight invoice manifest must pass. Check returns its problems, or none. */
interface AdaptiveFreightInvoiceManifestRule {
    fun check(item: AdaptiveFreightInvoiceManifest): MutableList<String>
}

data class AdaptiveFreightInvoiceManifestRequiredShiftCodeRule(
    var message: String,
) : AdaptiveFreightInvoiceManifestRule {
    override fun check(item: AdaptiveFreightInvoiceManifest): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveFreightInvoiceManifestCapacityRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveFreightInvoiceManifestRule {
    override fun check(item: AdaptiveFreightInvoiceManifest): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveFreightInvoiceManifestCodePrefixRule(
    var prefix: String,
) : AdaptiveFreightInvoiceManifestRule {
    override fun check(item: AdaptiveFreightInvoiceManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive freight invoice manifest. */
data class AdaptiveFreightInvoiceManifestValidator(var rules: MutableList<AdaptiveFreightInvoiceManifestRule>) {
    fun validate(item: AdaptiveFreightInvoiceManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveFreightInvoiceManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveFreightInvoiceManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveFreightInvoiceManifestValidator(): AdaptiveFreightInvoiceManifestValidator {
    return AdaptiveFreightInvoiceManifestValidator(
        mutableListOf<AdaptiveFreightInvoiceManifestRule>(
            AdaptiveFreightInvoiceManifestRequiredShiftCodeRule(
                "adaptive freight invoice manifest needs this field",
            ),
            AdaptiveFreightInvoiceManifestCapacityRangeRule(255, 1000),
            AdaptiveFreightInvoiceManifestCodePrefixRule("REF"),
        ),
    )
}
