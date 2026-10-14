package com.acme.transport.safety.scorecard.bundle

/** A rule a scorecard bundle must pass. Check returns its problems, or none. */
interface ScorecardBundleRule {
    fun check(item: ScorecardBundle): MutableList<String>
}

data class ScorecardBundleFuelLitersRangeRule(var min: Double, var max: Double) : ScorecardBundleRule {
    override fun check(item: ScorecardBundle): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class ScorecardBundleMinimumRangeRule(var min: Double, var max: Double) : ScorecardBundleRule {
    override fun check(item: ScorecardBundle): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class ScorecardBundleVisibleGuardRule(var limit: Double) : ScorecardBundleRule {
    override fun check(item: ScorecardBundle): MutableList<String> {
        if (item.visible && item.minimum > limit) {
            return mutableListOf<String>("minimum ${item.minimum} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardBundleAmountRangeRule(var min: Double, var max: Double) : ScorecardBundleRule {
    override fun check(item: ScorecardBundle): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard bundle. */
data class ScorecardBundleValidator(var rules: MutableList<ScorecardBundleRule>) {
    fun validate(item: ScorecardBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScorecardBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardBundleValidator(): ScorecardBundleValidator {
    return ScorecardBundleValidator(
        mutableListOf<ScorecardBundleRule>(
            ScorecardBundleFuelLitersRangeRule(1.6, 513.0),
            ScorecardBundleMinimumRangeRule(5.4, 1474.9),
            ScorecardBundleVisibleGuardRule(5.5),
            ScorecardBundleAmountRangeRule(8.6, 168.1),
        ),
    )
}
