package com.acme.transport.emissions.carbonreport.estimate

/** A rule a carbon report estimate must pass. Check returns its problems, or none. */
interface CarbonReportEstimateRule {
    fun check(item: CarbonReportEstimate): MutableList<String>
}

data class CarbonReportEstimateRequiredReviewerRule(var message: String) : CarbonReportEstimateRule {
    override fun check(item: CarbonReportEstimate): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportEstimateRequiredCodeRule(var message: String) : CarbonReportEstimateRule {
    override fun check(item: CarbonReportEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportEstimateGraceDaysRangeRule(var min: Int, var max: Int) : CarbonReportEstimateRule {
    override fun check(item: CarbonReportEstimate): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportEstimateRequiredDescriptionRule(var message: String) : CarbonReportEstimateRule {
    override fun check(item: CarbonReportEstimate): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportEstimateTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : CarbonReportEstimateRule {
    override fun check(item: CarbonReportEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class CarbonReportEstimateDescriptionLengthRule(var maxLength: Int) : CarbonReportEstimateRule {
    override fun check(item: CarbonReportEstimate): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report estimate. */
data class CarbonReportEstimateValidator(var rules: MutableList<CarbonReportEstimateRule>) {
    fun validate(item: CarbonReportEstimate): MutableList<String> {
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

    fun requireValid(item: CarbonReportEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportEstimateValidator(): CarbonReportEstimateValidator {
    return CarbonReportEstimateValidator(
        mutableListOf<CarbonReportEstimateRule>(
            CarbonReportEstimateRequiredReviewerRule("carbon report estimate needs an entry"),
            CarbonReportEstimateRequiredCodeRule("carbon report estimate needs a value"),
            CarbonReportEstimateGraceDaysRangeRule(120, 255),
            CarbonReportEstimateRequiredDescriptionRule("carbon report estimate needs this field"),
            CarbonReportEstimateTagsLimitRule(80, mutableListOf<String>()),
            CarbonReportEstimateDescriptionLengthRule(32),
        ),
    )
}
