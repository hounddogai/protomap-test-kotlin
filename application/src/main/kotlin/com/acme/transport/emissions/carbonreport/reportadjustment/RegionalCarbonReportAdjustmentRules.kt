package com.acme.transport.emissions.carbonreport.reportadjustment

/** A rule a regional carbon report adjustment must pass. Check returns its problems, or none. */
interface RegionalCarbonReportAdjustmentRule {
    fun check(item: RegionalCarbonReportAdjustment): MutableList<String>
}

data class RegionalCarbonReportAdjustmentRequiredCodeRule(
    var message: String,
) : RegionalCarbonReportAdjustmentRule {
    override fun check(item: RegionalCarbonReportAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportAdjustmentNameLengthRule(
    var maxLength: Int,
) : RegionalCarbonReportAdjustmentRule {
    override fun check(item: RegionalCarbonReportAdjustment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCarbonReportAdjustmentCodePrefixRule(
    var prefix: String,
) : RegionalCarbonReportAdjustmentRule {
    override fun check(item: RegionalCarbonReportAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional carbon report adjustment. */
data class RegionalCarbonReportAdjustmentValidator(var rules: MutableList<RegionalCarbonReportAdjustmentRule>) {
    fun validate(item: RegionalCarbonReportAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalCarbonReportAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCarbonReportAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCarbonReportAdjustmentValidator(): RegionalCarbonReportAdjustmentValidator {
    return RegionalCarbonReportAdjustmentValidator(
        mutableListOf<RegionalCarbonReportAdjustmentRule>(
            RegionalCarbonReportAdjustmentRequiredCodeRule("regional carbon report adjustment needs this field"),
            RegionalCarbonReportAdjustmentNameLengthRule(86400),
            RegionalCarbonReportAdjustmentCodePrefixRule("Q"),
        ),
    )
}
