package com.acme.transport.emissions.carbonreport.strictreportadjustment

/** A rule a global carbon report record must pass. Check returns its problems, or none. */
interface GlobalCarbonReportRecordRule {
    fun check(item: GlobalCarbonReportRecord): MutableList<String>
}

data class GlobalCarbonReportRecordScoreRangeRule(
    var min: Double,
    var max: Double,
) : GlobalCarbonReportRecordRule {
    override fun check(item: GlobalCarbonReportRecord): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCarbonReportRecordCodePrefixRule(var prefix: String) : GlobalCarbonReportRecordRule {
    override fun check(item: GlobalCarbonReportRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCarbonReportRecordCodeLengthRule(var maxLength: Int) : GlobalCarbonReportRecordRule {
    override fun check(item: GlobalCarbonReportRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCarbonReportRecordTrailerIdLengthRule(var maxLength: Int) : GlobalCarbonReportRecordRule {
    override fun check(item: GlobalCarbonReportRecord): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global carbon report record. */
data class GlobalCarbonReportRecordValidator(var rules: MutableList<GlobalCarbonReportRecordRule>) {
    fun validate(item: GlobalCarbonReportRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalCarbonReportRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalCarbonReportRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalCarbonReportRecordValidator(): GlobalCarbonReportRecordValidator {
    return GlobalCarbonReportRecordValidator(
        mutableListOf<GlobalCarbonReportRecordRule>(
            GlobalCarbonReportRecordScoreRangeRule(2.6, 2011.2),
            GlobalCarbonReportRecordCodePrefixRule("OPS"),
            GlobalCarbonReportRecordCodeLengthRule(120),
            GlobalCarbonReportRecordTrailerIdLengthRule(86400),
        ),
    )
}
