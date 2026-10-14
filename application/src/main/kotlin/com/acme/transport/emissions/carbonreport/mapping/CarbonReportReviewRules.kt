package com.acme.transport.emissions.carbonreport.mapping

import com.acme.transport.kit.text.normalize

/** A rule a carbon report review must pass. Check returns its problems, or none. */
interface CarbonReportReviewRule {
    fun check(item: CarbonReportReview): MutableList<String>
}

data class CarbonReportReviewRequiredLabelRule(var message: String) : CarbonReportReviewRule {
    override fun check(item: CarbonReportReview): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportReviewLabelLengthRule(var maxLength: Int) : CarbonReportReviewRule {
    override fun check(item: CarbonReportReview): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportReviewAllowedDockRule(var allowed: MutableList<String>) : CarbonReportReviewRule {
    override fun check(item: CarbonReportReview): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportReviewCodeLengthRule(var maxLength: Int) : CarbonReportReviewRule {
    override fun check(item: CarbonReportReview): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportReviewRequiredSummaryRule(var message: String) : CarbonReportReviewRule {
    override fun check(item: CarbonReportReview): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportReviewLoadKgRangeRule(var min: Double, var max: Double) : CarbonReportReviewRule {
    override fun check(item: CarbonReportReview): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a carbon report review. */
data class CarbonReportReviewValidator(var rules: MutableList<CarbonReportReviewRule>) {
    fun validate(item: CarbonReportReview): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CarbonReportReview) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportReview>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportReviewValidator(): CarbonReportReviewValidator {
    return CarbonReportReviewValidator(
        mutableListOf<CarbonReportReviewRule>(
            CarbonReportReviewRequiredLabelRule("carbon report review needs this field"),
            CarbonReportReviewLabelLengthRule(80),
            CarbonReportReviewAllowedDockRule(mutableListOf<String>("regional")),
            CarbonReportReviewCodeLengthRule(3600),
            CarbonReportReviewRequiredSummaryRule("carbon report review needs a value"),
            CarbonReportReviewLoadKgRangeRule(8.2, 80.5),
        ),
    )
}
