package com.acme.transport.safety.scorecard.scorecardbundle

/** A rule a primary scorecard bundle must pass. Check returns its problems, or none. */
interface PrimaryScorecardBundleRule {
    fun check(item: PrimaryScorecardBundle): MutableList<String>
}

data class PrimaryScorecardBundleCategoryLengthRule(var maxLength: Int) : PrimaryScorecardBundleRule {
    override fun check(item: PrimaryScorecardBundle): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryScorecardBundlePalletsRangeRule(var min: Int, var max: Int) : PrimaryScorecardBundleRule {
    override fun check(item: PrimaryScorecardBundle): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class PrimaryScorecardBundleRequiredSegmentRule(var message: String) : PrimaryScorecardBundleRule {
    override fun check(item: PrimaryScorecardBundle): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryScorecardBundleMaximumRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryScorecardBundleRule {
    override fun check(item: PrimaryScorecardBundle): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryScorecardBundleTitleLengthRule(var maxLength: Int) : PrimaryScorecardBundleRule {
    override fun check(item: PrimaryScorecardBundle): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary scorecard bundle. */
data class PrimaryScorecardBundleValidator(var rules: MutableList<PrimaryScorecardBundleRule>) {
    fun validate(item: PrimaryScorecardBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryScorecardBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryScorecardBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryScorecardBundleValidator(): PrimaryScorecardBundleValidator {
    return PrimaryScorecardBundleValidator(
        mutableListOf<PrimaryScorecardBundleRule>(
            PrimaryScorecardBundleCategoryLengthRule(3600),
            PrimaryScorecardBundlePalletsRangeRule(32, 1000),
            PrimaryScorecardBundleRequiredSegmentRule("primary scorecard bundle needs a value"),
            PrimaryScorecardBundleMaximumRangeRule(1.3, 469.7),
            PrimaryScorecardBundleTitleLengthRule(86400),
        ),
    )
}
