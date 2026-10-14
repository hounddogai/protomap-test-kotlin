package com.acme.transport.emissions.carbonreport.reportagreement

/** A rule a lenient carbon report manifest must pass. Check returns its problems, or none. */
interface LenientCarbonReportManifestRule {
    fun check(item: LenientCarbonReportManifest): MutableList<String>
}

data class LenientCarbonReportManifestDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : LenientCarbonReportManifestRule {
    override fun check(item: LenientCarbonReportManifest): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientCarbonReportManifestRequiredLabelRule(var message: String) : LenientCarbonReportManifestRule {
    override fun check(item: LenientCarbonReportManifest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientCarbonReportManifestSummaryLengthRule(var maxLength: Int) : LenientCarbonReportManifestRule {
    override fun check(item: LenientCarbonReportManifest): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCarbonReportManifestLabelLengthRule(var maxLength: Int) : LenientCarbonReportManifestRule {
    override fun check(item: LenientCarbonReportManifest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient carbon report manifest. */
data class LenientCarbonReportManifestValidator(var rules: MutableList<LenientCarbonReportManifestRule>) {
    fun validate(item: LenientCarbonReportManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientCarbonReportManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientCarbonReportManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientCarbonReportManifestValidator(): LenientCarbonReportManifestValidator {
    return LenientCarbonReportManifestValidator(
        mutableListOf<LenientCarbonReportManifestRule>(
            LenientCarbonReportManifestDurationMinutesRangeRule(80, 120),
            LenientCarbonReportManifestRequiredLabelRule("lenient carbon report manifest needs a value"),
            LenientCarbonReportManifestSummaryLengthRule(3600),
            LenientCarbonReportManifestLabelLengthRule(120),
        ),
    )
}
