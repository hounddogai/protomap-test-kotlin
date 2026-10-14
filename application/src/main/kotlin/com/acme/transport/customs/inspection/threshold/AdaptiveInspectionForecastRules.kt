package com.acme.transport.customs.inspection.threshold

/** A rule a adaptive inspection forecast must pass. Check returns its problems, or none. */
interface AdaptiveInspectionForecastRule {
    fun check(item: AdaptiveInspectionForecast): MutableList<String>
}

data class AdaptiveInspectionForecastCodePrefixRule(var prefix: String) : AdaptiveInspectionForecastRule {
    override fun check(item: AdaptiveInspectionForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionForecastTrailerIdLengthRule(var maxLength: Int) : AdaptiveInspectionForecastRule {
    override fun check(item: AdaptiveInspectionForecast): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionForecastLabelLengthRule(var maxLength: Int) : AdaptiveInspectionForecastRule {
    override fun check(item: AdaptiveInspectionForecast): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionForecastVersionRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveInspectionForecastRule {
    override fun check(item: AdaptiveInspectionForecast): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class AdaptiveInspectionForecastRequiredTitleRule(var message: String) : AdaptiveInspectionForecastRule {
    override fun check(item: AdaptiveInspectionForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive inspection forecast. */
data class AdaptiveInspectionForecastValidator(var rules: MutableList<AdaptiveInspectionForecastRule>) {
    fun validate(item: AdaptiveInspectionForecast): MutableList<String> {
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

    fun requireValid(item: AdaptiveInspectionForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveInspectionForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveInspectionForecastValidator(): AdaptiveInspectionForecastValidator {
    return AdaptiveInspectionForecastValidator(
        mutableListOf<AdaptiveInspectionForecastRule>(
            AdaptiveInspectionForecastCodePrefixRule("OPS"),
            AdaptiveInspectionForecastTrailerIdLengthRule(80),
            AdaptiveInspectionForecastLabelLengthRule(120),
            AdaptiveInspectionForecastVersionRangeRule(32, 120),
            AdaptiveInspectionForecastRequiredTitleRule("adaptive inspection forecast needs this field"),
        ),
    )
}
