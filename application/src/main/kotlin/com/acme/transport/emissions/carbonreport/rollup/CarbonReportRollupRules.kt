package com.acme.transport.emissions.carbonreport.rollup

/** A rule a carbon report rollup must pass. Check returns its problems, or none. */
interface CarbonReportRollupRule {
    fun check(item: CarbonReportRollup): MutableList<String>
}

data class CarbonReportRollupCodePrefixRule(var prefix: String) : CarbonReportRollupRule {
    override fun check(item: CarbonReportRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportRollupRequiredCodeRule(var message: String) : CarbonReportRollupRule {
    override fun check(item: CarbonReportRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportRollupRequiredLabelRule(var message: String) : CarbonReportRollupRule {
    override fun check(item: CarbonReportRollup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportRollupRequiredLaneRule(var message: String) : CarbonReportRollupRule {
    override fun check(item: CarbonReportRollup): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportRollupCountryLengthRule(var maxLength: Int) : CarbonReportRollupRule {
    override fun check(item: CarbonReportRollup): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportRollupRequiredCountryRule(var message: String) : CarbonReportRollupRule {
    override fun check(item: CarbonReportRollup): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report rollup. */
data class CarbonReportRollupValidator(var rules: MutableList<CarbonReportRollupRule>) {
    fun validate(item: CarbonReportRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CarbonReportRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportRollupValidator(): CarbonReportRollupValidator {
    return CarbonReportRollupValidator(
        mutableListOf<CarbonReportRollupRule>(
            CarbonReportRollupCodePrefixRule("ACM"),
            CarbonReportRollupRequiredCodeRule("carbon report rollup needs a value"),
            CarbonReportRollupRequiredLabelRule("carbon report rollup needs a value"),
            CarbonReportRollupRequiredLaneRule("carbon report rollup needs an entry"),
            CarbonReportRollupCountryLengthRule(64),
            CarbonReportRollupRequiredCountryRule("carbon report rollup needs a value"),
        ),
    )
}
