package com.acme.transport.emissions.carbonreport.allocation

/** A rule a carbon report manifest must pass. Check returns its problems, or none. */
interface CarbonReportManifestRule {
    fun check(item: CarbonReportManifest): MutableList<String>
}

data class CarbonReportManifestTitleLengthRule(var maxLength: Int) : CarbonReportManifestRule {
    override fun check(item: CarbonReportManifest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportManifestRequiredLocaleRule(var message: String) : CarbonReportManifestRule {
    override fun check(item: CarbonReportManifest): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportManifestCodeLengthRule(var maxLength: Int) : CarbonReportManifestRule {
    override fun check(item: CarbonReportManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report manifest. */
data class CarbonReportManifestValidator(var rules: MutableList<CarbonReportManifestRule>) {
    fun validate(item: CarbonReportManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CarbonReportManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportManifestValidator(): CarbonReportManifestValidator {
    return CarbonReportManifestValidator(
        mutableListOf<CarbonReportManifestRule>(
            CarbonReportManifestTitleLengthRule(120),
            CarbonReportManifestRequiredLocaleRule("carbon report manifest needs this field"),
            CarbonReportManifestCodeLengthRule(3600),
        ),
    )
}
