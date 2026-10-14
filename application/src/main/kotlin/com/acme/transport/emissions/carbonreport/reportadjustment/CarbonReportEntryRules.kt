package com.acme.transport.emissions.carbonreport.reportadjustment

/** A rule a carbon report entry must pass. Check returns its problems, or none. */
interface CarbonReportEntryRule {
    fun check(item: CarbonReportEntry): MutableList<String>
}

data class CarbonReportEntryDockLengthRule(var maxLength: Int) : CarbonReportEntryRule {
    override fun check(item: CarbonReportEntry): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportEntryRequiredLabelRule(var message: String) : CarbonReportEntryRule {
    override fun check(item: CarbonReportEntry): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportEntryLabelLengthRule(var maxLength: Int) : CarbonReportEntryRule {
    override fun check(item: CarbonReportEntry): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportEntryCodeLengthRule(var maxLength: Int) : CarbonReportEntryRule {
    override fun check(item: CarbonReportEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportEntryRouteCodeLengthRule(var maxLength: Int) : CarbonReportEntryRule {
    override fun check(item: CarbonReportEntry): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report entry. */
data class CarbonReportEntryValidator(var rules: MutableList<CarbonReportEntryRule>) {
    fun validate(item: CarbonReportEntry): MutableList<String> {
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

    fun requireValid(item: CarbonReportEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportEntryValidator(): CarbonReportEntryValidator {
    return CarbonReportEntryValidator(
        mutableListOf<CarbonReportEntryRule>(
            CarbonReportEntryDockLengthRule(16),
            CarbonReportEntryRequiredLabelRule("carbon report entry needs this field"),
            CarbonReportEntryLabelLengthRule(86400),
            CarbonReportEntryCodeLengthRule(3600),
            CarbonReportEntryRouteCodeLengthRule(120),
        ),
    )
}
