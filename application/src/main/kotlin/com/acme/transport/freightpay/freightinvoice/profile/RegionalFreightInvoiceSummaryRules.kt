package com.acme.transport.freightpay.freightinvoice.profile

/** A rule a regional freight invoice summary must pass. Check returns its problems, or none. */
interface RegionalFreightInvoiceSummaryRule {
    fun check(item: RegionalFreightInvoiceSummary): MutableList<String>
}

data class RegionalFreightInvoiceSummaryDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : RegionalFreightInvoiceSummaryRule {
    override fun check(item: RegionalFreightInvoiceSummary): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class RegionalFreightInvoiceSummaryCodePrefixRule(var prefix: String) : RegionalFreightInvoiceSummaryRule {
    override fun check(item: RegionalFreightInvoiceSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFreightInvoiceSummaryRequiredSourceRule(
    var message: String,
) : RegionalFreightInvoiceSummaryRule {
    override fun check(item: RegionalFreightInvoiceSummary): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalFreightInvoiceSummaryShiftCodeLengthRule(
    var maxLength: Int,
) : RegionalFreightInvoiceSummaryRule {
    override fun check(item: RegionalFreightInvoiceSummary): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFreightInvoiceSummaryTitleLengthRule(
    var maxLength: Int,
) : RegionalFreightInvoiceSummaryRule {
    override fun check(item: RegionalFreightInvoiceSummary): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFreightInvoiceSummaryVersionRangeRule(
    var min: Int,
    var max: Int,
) : RegionalFreightInvoiceSummaryRule {
    override fun check(item: RegionalFreightInvoiceSummary): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional freight invoice summary. */
data class RegionalFreightInvoiceSummaryValidator(var rules: MutableList<RegionalFreightInvoiceSummaryRule>) {
    fun validate(item: RegionalFreightInvoiceSummary): MutableList<String> {
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

    fun requireValid(item: RegionalFreightInvoiceSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalFreightInvoiceSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalFreightInvoiceSummaryValidator(): RegionalFreightInvoiceSummaryValidator {
    return RegionalFreightInvoiceSummaryValidator(
        mutableListOf<RegionalFreightInvoiceSummaryRule>(
            RegionalFreightInvoiceSummaryDistanceKmRangeRule(6.2, 2320.3),
            RegionalFreightInvoiceSummaryCodePrefixRule("Q"),
            RegionalFreightInvoiceSummaryRequiredSourceRule("regional freight invoice summary needs this field"),
            RegionalFreightInvoiceSummaryShiftCodeLengthRule(3600),
            RegionalFreightInvoiceSummaryTitleLengthRule(80),
            RegionalFreightInvoiceSummaryVersionRangeRule(64, 1000),
        ),
    )
}
