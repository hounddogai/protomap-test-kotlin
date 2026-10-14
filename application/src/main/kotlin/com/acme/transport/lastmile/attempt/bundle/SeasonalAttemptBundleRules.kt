package com.acme.transport.lastmile.attempt.bundle

/** A rule a seasonal attempt bundle must pass. Check returns its problems, or none. */
interface SeasonalAttemptBundleRule {
    fun check(item: SeasonalAttemptBundle): MutableList<String>
}

data class SeasonalAttemptBundleCodePrefixRule(var prefix: String) : SeasonalAttemptBundleRule {
    override fun check(item: SeasonalAttemptBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAttemptBundleRequiredLabelRule(var message: String) : SeasonalAttemptBundleRule {
    override fun check(item: SeasonalAttemptBundle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAttemptBundleEtaMinutesRangeRule(var min: Int, var max: Int) : SeasonalAttemptBundleRule {
    override fun check(item: SeasonalAttemptBundle): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal attempt bundle. */
data class SeasonalAttemptBundleValidator(var rules: MutableList<SeasonalAttemptBundleRule>) {
    fun validate(item: SeasonalAttemptBundle): MutableList<String> {
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

    fun requireValid(item: SeasonalAttemptBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalAttemptBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalAttemptBundleValidator(): SeasonalAttemptBundleValidator {
    return SeasonalAttemptBundleValidator(
        mutableListOf<SeasonalAttemptBundleRule>(
            SeasonalAttemptBundleCodePrefixRule("ACM"),
            SeasonalAttemptBundleRequiredLabelRule("seasonal attempt bundle needs an entry"),
            SeasonalAttemptBundleEtaMinutesRangeRule(16, 80),
        ),
    )
}
