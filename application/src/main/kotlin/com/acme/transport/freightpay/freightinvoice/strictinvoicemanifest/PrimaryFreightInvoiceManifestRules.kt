package com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest

/** A rule a primary freight invoice manifest must pass. Check returns its problems, or none. */
interface PrimaryFreightInvoiceManifestRule {
    fun check(item: PrimaryFreightInvoiceManifest): MutableList<String>
}

data class PrimaryFreightInvoiceManifestNameLengthRule(var maxLength: Int) : PrimaryFreightInvoiceManifestRule {
    override fun check(item: PrimaryFreightInvoiceManifest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFreightInvoiceManifestRequiredNameRule(
    var message: String,
) : PrimaryFreightInvoiceManifestRule {
    override fun check(item: PrimaryFreightInvoiceManifest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFreightInvoiceManifestRatioRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryFreightInvoiceManifestRule {
    override fun check(item: PrimaryFreightInvoiceManifest): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class PrimaryFreightInvoiceManifestRequiredTitleRule(
    var message: String,
) : PrimaryFreightInvoiceManifestRule {
    override fun check(item: PrimaryFreightInvoiceManifest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary freight invoice manifest. */
data class PrimaryFreightInvoiceManifestValidator(var rules: MutableList<PrimaryFreightInvoiceManifestRule>) {
    fun validate(item: PrimaryFreightInvoiceManifest): MutableList<String> {
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

    fun requireValid(item: PrimaryFreightInvoiceManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryFreightInvoiceManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryFreightInvoiceManifestValidator(): PrimaryFreightInvoiceManifestValidator {
    return PrimaryFreightInvoiceManifestValidator(
        mutableListOf<PrimaryFreightInvoiceManifestRule>(
            PrimaryFreightInvoiceManifestNameLengthRule(16),
            PrimaryFreightInvoiceManifestRequiredNameRule("primary freight invoice manifest needs this field"),
            PrimaryFreightInvoiceManifestRatioRangeRule(5.5, 1817.3),
            PrimaryFreightInvoiceManifestRequiredTitleRule("primary freight invoice manifest needs a value"),
        ),
    )
}
