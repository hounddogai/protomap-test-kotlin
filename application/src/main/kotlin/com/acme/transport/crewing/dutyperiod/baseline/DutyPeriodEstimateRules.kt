package com.acme.transport.crewing.dutyperiod.baseline

import com.acme.transport.kit.text.normalize

/** A rule a duty period estimate must pass. Check returns its problems, or none. */
interface DutyPeriodEstimateRule {
    fun check(item: DutyPeriodEstimate): MutableList<String>
}

data class DutyPeriodEstimateRequiredCategoryRule(var message: String) : DutyPeriodEstimateRule {
    override fun check(item: DutyPeriodEstimate): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodEstimateAllowedCodeRule(var allowed: MutableList<String>) : DutyPeriodEstimateRule {
    override fun check(item: DutyPeriodEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodEstimateLabelLengthRule(var maxLength: Int) : DutyPeriodEstimateRule {
    override fun check(item: DutyPeriodEstimate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period estimate. */
data class DutyPeriodEstimateValidator(var rules: MutableList<DutyPeriodEstimateRule>) {
    fun validate(item: DutyPeriodEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DutyPeriodEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodEstimateValidator(): DutyPeriodEstimateValidator {
    return DutyPeriodEstimateValidator(
        mutableListOf<DutyPeriodEstimateRule>(
            DutyPeriodEstimateRequiredCategoryRule("duty period estimate needs a value"),
            DutyPeriodEstimateAllowedCodeRule(mutableListOf<String>("global", "primary")),
            DutyPeriodEstimateLabelLengthRule(120),
        ),
    )
}
