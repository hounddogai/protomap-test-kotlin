package com.acme.transport.emissions.carbonreport.reportscore

/** A rule a default carbon report mapping must pass. Check returns its problems, or none. */
interface DefaultCarbonReportMappingRule {
    fun check(item: DefaultCarbonReportMapping): MutableList<String>
}

data class DefaultCarbonReportMappingRequiredSegmentRule(var message: String) : DefaultCarbonReportMappingRule {
    override fun check(item: DefaultCarbonReportMapping): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCarbonReportMappingRequiredTitleRule(var message: String) : DefaultCarbonReportMappingRule {
    override fun check(item: DefaultCarbonReportMapping): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCarbonReportMappingRequiredSummaryRule(var message: String) : DefaultCarbonReportMappingRule {
    override fun check(item: DefaultCarbonReportMapping): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCarbonReportMappingSummaryLengthRule(var maxLength: Int) : DefaultCarbonReportMappingRule {
    override fun check(item: DefaultCarbonReportMapping): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCarbonReportMappingSegmentLengthRule(var maxLength: Int) : DefaultCarbonReportMappingRule {
    override fun check(item: DefaultCarbonReportMapping): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCarbonReportMappingCodeLengthRule(var maxLength: Int) : DefaultCarbonReportMappingRule {
    override fun check(item: DefaultCarbonReportMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default carbon report mapping. */
data class DefaultCarbonReportMappingValidator(var rules: MutableList<DefaultCarbonReportMappingRule>) {
    fun validate(item: DefaultCarbonReportMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultCarbonReportMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultCarbonReportMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultCarbonReportMappingValidator(): DefaultCarbonReportMappingValidator {
    return DefaultCarbonReportMappingValidator(
        mutableListOf<DefaultCarbonReportMappingRule>(
            DefaultCarbonReportMappingRequiredSegmentRule("default carbon report mapping needs this field"),
            DefaultCarbonReportMappingRequiredTitleRule("default carbon report mapping needs a value"),
            DefaultCarbonReportMappingRequiredSummaryRule("default carbon report mapping needs this field"),
            DefaultCarbonReportMappingSummaryLengthRule(255),
            DefaultCarbonReportMappingSegmentLengthRule(32),
            DefaultCarbonReportMappingCodeLengthRule(16),
        ),
    )
}
