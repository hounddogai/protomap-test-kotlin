package com.acme.transport.emissions.carbonreport.entry

/** A rule a tiered carbon report entry must pass. Check returns its problems, or none. */
interface TieredCarbonReportEntryRule {
    fun check(item: TieredCarbonReportEntry): MutableList<String>
}

data class TieredCarbonReportEntryRequiredRouteCodeRule(var message: String) : TieredCarbonReportEntryRule {
    override fun check(item: TieredCarbonReportEntry): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCarbonReportEntryCodeLengthRule(var maxLength: Int) : TieredCarbonReportEntryRule {
    override fun check(item: TieredCarbonReportEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCarbonReportEntryRequiredCodeRule(var message: String) : TieredCarbonReportEntryRule {
    override fun check(item: TieredCarbonReportEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCarbonReportEntryRequiredTitleRule(var message: String) : TieredCarbonReportEntryRule {
    override fun check(item: TieredCarbonReportEntry): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered carbon report entry. */
data class TieredCarbonReportEntryValidator(var rules: MutableList<TieredCarbonReportEntryRule>) {
    fun validate(item: TieredCarbonReportEntry): MutableList<String> {
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

    fun requireValid(item: TieredCarbonReportEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCarbonReportEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCarbonReportEntryValidator(): TieredCarbonReportEntryValidator {
    return TieredCarbonReportEntryValidator(
        mutableListOf<TieredCarbonReportEntryRule>(
            TieredCarbonReportEntryRequiredRouteCodeRule("tiered carbon report entry needs this field"),
            TieredCarbonReportEntryCodeLengthRule(86400),
            TieredCarbonReportEntryRequiredCodeRule("tiered carbon report entry needs a value"),
            TieredCarbonReportEntryRequiredTitleRule("tiered carbon report entry needs this field"),
        ),
    )
}
