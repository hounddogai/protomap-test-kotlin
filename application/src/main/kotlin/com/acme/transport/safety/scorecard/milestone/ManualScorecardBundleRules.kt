package com.acme.transport.safety.scorecard.milestone

/** A rule a manual scorecard bundle must pass. Check returns its problems, or none. */
interface ManualScorecardBundleRule {
    fun check(item: ManualScorecardBundle): MutableList<String>
}

data class ManualScorecardBundleEtaMinutesRangeRule(var min: Int, var max: Int) : ManualScorecardBundleRule {
    override fun check(item: ManualScorecardBundle): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class ManualScorecardBundleRequiredCodeRule(var message: String) : ManualScorecardBundleRule {
    override fun check(item: ManualScorecardBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualScorecardBundleSummaryLengthRule(var maxLength: Int) : ManualScorecardBundleRule {
    override fun check(item: ManualScorecardBundle): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual scorecard bundle. */
data class ManualScorecardBundleValidator(var rules: MutableList<ManualScorecardBundleRule>) {
    fun validate(item: ManualScorecardBundle): MutableList<String> {
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

    fun requireValid(item: ManualScorecardBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualScorecardBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultManualScorecardBundleValidator(): ManualScorecardBundleValidator {
    return ManualScorecardBundleValidator(
        mutableListOf<ManualScorecardBundleRule>(
            ManualScorecardBundleEtaMinutesRangeRule(32, 64),
            ManualScorecardBundleRequiredCodeRule("manual scorecard bundle needs an entry"),
            ManualScorecardBundleSummaryLengthRule(80),
        ),
    )
}
