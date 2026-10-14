package com.acme.transport.emissions.carbonreport.threshold

/** A rule a carbon report threshold must pass. Check returns its problems, or none. */
interface CarbonReportThresholdRule {
    fun check(item: CarbonReportThreshold): MutableList<String>
}

data class CarbonReportThresholdRequiredRegionRule(var message: String) : CarbonReportThresholdRule {
    override fun check(item: CarbonReportThreshold): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportThresholdRegionLengthRule(var maxLength: Int) : CarbonReportThresholdRule {
    override fun check(item: CarbonReportThreshold): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportThresholdRequiredCodeRule(var message: String) : CarbonReportThresholdRule {
    override fun check(item: CarbonReportThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report threshold. */
data class CarbonReportThresholdValidator(var rules: MutableList<CarbonReportThresholdRule>) {
    fun validate(item: CarbonReportThreshold): MutableList<String> {
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

    fun requireValid(item: CarbonReportThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportThresholdValidator(): CarbonReportThresholdValidator {
    return CarbonReportThresholdValidator(
        mutableListOf<CarbonReportThresholdRule>(
            CarbonReportThresholdRequiredRegionRule("carbon report threshold needs an entry"),
            CarbonReportThresholdRegionLengthRule(255),
            CarbonReportThresholdRequiredCodeRule("carbon report threshold needs an entry"),
        ),
    )
}
