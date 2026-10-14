package com.acme.transport.safety.coaching.coachingtier

/** A rule a lenient coaching forecast must pass. Check returns its problems, or none. */
interface LenientCoachingForecastRule {
    fun check(item: LenientCoachingForecast): MutableList<String>
}

data class LenientCoachingForecastApprovedGuardRule(var limit: Int) : LenientCoachingForecastRule {
    override fun check(item: LenientCoachingForecast): MutableList<String> {
        if (item.approved && item.durationMinutes > limit) {
            return mutableListOf<String>("duration minutes ${item.durationMinutes} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class LenientCoachingForecastThresholdRangeRule(
    var min: Double,
    var max: Double,
) : LenientCoachingForecastRule {
    override fun check(item: LenientCoachingForecast): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientCoachingForecastStartsAtRangeRule(var min: Int, var max: Int) : LenientCoachingForecastRule {
    override fun check(item: LenientCoachingForecast): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientCoachingForecastCodeLengthRule(var maxLength: Int) : LenientCoachingForecastRule {
    override fun check(item: LenientCoachingForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCoachingForecastRequiredTitleRule(var message: String) : LenientCoachingForecastRule {
    override fun check(item: LenientCoachingForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientCoachingForecastRequiredExternalRefRule(var message: String) : LenientCoachingForecastRule {
    override fun check(item: LenientCoachingForecast): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient coaching forecast. */
data class LenientCoachingForecastValidator(var rules: MutableList<LenientCoachingForecastRule>) {
    fun validate(item: LenientCoachingForecast): MutableList<String> {
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

    fun requireValid(item: LenientCoachingForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientCoachingForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientCoachingForecastValidator(): LenientCoachingForecastValidator {
    return LenientCoachingForecastValidator(
        mutableListOf<LenientCoachingForecastRule>(
            LenientCoachingForecastApprovedGuardRule(64),
            LenientCoachingForecastThresholdRangeRule(7.1, 4806.8),
            LenientCoachingForecastStartsAtRangeRule(32, 3600),
            LenientCoachingForecastCodeLengthRule(64),
            LenientCoachingForecastRequiredTitleRule("lenient coaching forecast needs an entry"),
            LenientCoachingForecastRequiredExternalRefRule("lenient coaching forecast needs a value"),
        ),
    )
}
