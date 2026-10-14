package com.acme.transport.crewing.dutyperiod.estimate

import com.acme.transport.kit.text.normalize

/** A rule a strict duty period estimate must pass. Check returns its problems, or none. */
interface StrictDutyPeriodEstimateRule {
    fun check(item: StrictDutyPeriodEstimate): MutableList<String>
}

data class StrictDutyPeriodEstimateMarginRangeRule(
    var min: Double,
    var max: Double,
) : StrictDutyPeriodEstimateRule {
    override fun check(item: StrictDutyPeriodEstimate): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class StrictDutyPeriodEstimateAllowedCodeRule(
    var allowed: MutableList<String>,
) : StrictDutyPeriodEstimateRule {
    override fun check(item: StrictDutyPeriodEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class StrictDutyPeriodEstimateCountryLengthRule(var maxLength: Int) : StrictDutyPeriodEstimateRule {
    override fun check(item: StrictDutyPeriodEstimate): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDutyPeriodEstimateRequiredNameRule(var message: String) : StrictDutyPeriodEstimateRule {
    override fun check(item: StrictDutyPeriodEstimate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict duty period estimate. */
data class StrictDutyPeriodEstimateValidator(var rules: MutableList<StrictDutyPeriodEstimateRule>) {
    fun validate(item: StrictDutyPeriodEstimate): MutableList<String> {
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

    fun requireValid(item: StrictDutyPeriodEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDutyPeriodEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDutyPeriodEstimateValidator(): StrictDutyPeriodEstimateValidator {
    return StrictDutyPeriodEstimateValidator(
        mutableListOf<StrictDutyPeriodEstimateRule>(
            StrictDutyPeriodEstimateMarginRangeRule(0.6, 2674.6),
            StrictDutyPeriodEstimateAllowedCodeRule(mutableListOf<String>("weighted", "global", "batched")),
            StrictDutyPeriodEstimateCountryLengthRule(64),
            StrictDutyPeriodEstimateRequiredNameRule("strict duty period estimate needs an entry"),
        ),
    )
}
