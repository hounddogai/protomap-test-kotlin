package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.text.normalize

/** A rule a tiered camera event forecast must pass. Check returns its problems, or none. */
interface TieredCameraEventForecastRule {
    fun check(item: TieredCameraEventForecast): MutableList<String>
}

data class TieredCameraEventForecastRequiredNameRule(var message: String) : TieredCameraEventForecastRule {
    override fun check(item: TieredCameraEventForecast): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCameraEventForecastReviewerLengthRule(var maxLength: Int) : TieredCameraEventForecastRule {
    override fun check(item: TieredCameraEventForecast): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCameraEventForecastAllowedReviewerRule(
    var allowed: MutableList<String>,
) : TieredCameraEventForecastRule {
    override fun check(item: TieredCameraEventForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is too long")
        }
        return mutableListOf<String>()
    }
}

data class TieredCameraEventForecastMinimumRangeRule(
    var min: Double,
    var max: Double,
) : TieredCameraEventForecastRule {
    override fun check(item: TieredCameraEventForecast): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredCameraEventForecastExternalRefLengthRule(var maxLength: Int) : TieredCameraEventForecastRule {
    override fun check(item: TieredCameraEventForecast): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered camera event forecast. */
data class TieredCameraEventForecastValidator(var rules: MutableList<TieredCameraEventForecastRule>) {
    fun validate(item: TieredCameraEventForecast): MutableList<String> {
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

    fun requireValid(item: TieredCameraEventForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCameraEventForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCameraEventForecastValidator(): TieredCameraEventForecastValidator {
    return TieredCameraEventForecastValidator(
        mutableListOf<TieredCameraEventForecastRule>(
            TieredCameraEventForecastRequiredNameRule("tiered camera event forecast needs a value"),
            TieredCameraEventForecastReviewerLengthRule(32),
            TieredCameraEventForecastAllowedReviewerRule(
                mutableListOf<String>("batched", "regional", "pending"),
            ),
            TieredCameraEventForecastMinimumRangeRule(6.3, 273.6),
            TieredCameraEventForecastExternalRefLengthRule(1000),
        ),
    )
}
