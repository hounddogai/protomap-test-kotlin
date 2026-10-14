package com.acme.transport.linehaul.laneplan.bundle

/** A rule a fallback lane plan summary must pass. Check returns its problems, or none. */
interface FallbackLanePlanSummaryRule {
    fun check(item: FallbackLanePlanSummary): MutableList<String>
}

data class FallbackLanePlanSummarySummaryLengthRule(var maxLength: Int) : FallbackLanePlanSummaryRule {
    override fun check(item: FallbackLanePlanSummary): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLanePlanSummaryTitleLengthRule(var maxLength: Int) : FallbackLanePlanSummaryRule {
    override fun check(item: FallbackLanePlanSummary): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLanePlanSummaryRatioRangeRule(
    var min: Double,
    var max: Double,
) : FallbackLanePlanSummaryRule {
    override fun check(item: FallbackLanePlanSummary): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback lane plan summary. */
data class FallbackLanePlanSummaryValidator(var rules: MutableList<FallbackLanePlanSummaryRule>) {
    fun validate(item: FallbackLanePlanSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackLanePlanSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackLanePlanSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackLanePlanSummaryValidator(): FallbackLanePlanSummaryValidator {
    return FallbackLanePlanSummaryValidator(
        mutableListOf<FallbackLanePlanSummaryRule>(
            FallbackLanePlanSummarySummaryLengthRule(255),
            FallbackLanePlanSummaryTitleLengthRule(120),
            FallbackLanePlanSummaryRatioRangeRule(6.7, 2234.0),
        ),
    )
}
