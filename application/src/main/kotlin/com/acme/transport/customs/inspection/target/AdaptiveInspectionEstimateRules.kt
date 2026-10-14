package com.acme.transport.customs.inspection.target

/** A rule a adaptive inspection estimate must pass. Check returns its problems, or none. */
interface AdaptiveInspectionEstimateRule {
    fun check(item: AdaptiveInspectionEstimate): MutableList<String>
}

data class AdaptiveInspectionEstimateNameLengthRule(var maxLength: Int) : AdaptiveInspectionEstimateRule {
    override fun check(item: AdaptiveInspectionEstimate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionEstimateRequiredCodeRule(var message: String) : AdaptiveInspectionEstimateRule {
    override fun check(item: AdaptiveInspectionEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionEstimateAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveInspectionEstimateRule {
    override fun check(item: AdaptiveInspectionEstimate): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionEstimateRequiredSourceRule(var message: String) : AdaptiveInspectionEstimateRule {
    override fun check(item: AdaptiveInspectionEstimate): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionEstimateAmountRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveInspectionEstimateRule {
    override fun check(item: AdaptiveInspectionEstimate): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive inspection estimate. */
data class AdaptiveInspectionEstimateValidator(var rules: MutableList<AdaptiveInspectionEstimateRule>) {
    fun validate(item: AdaptiveInspectionEstimate): MutableList<String> {
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

    fun requireValid(item: AdaptiveInspectionEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveInspectionEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveInspectionEstimateValidator(): AdaptiveInspectionEstimateValidator {
    return AdaptiveInspectionEstimateValidator(
        mutableListOf<AdaptiveInspectionEstimateRule>(
            AdaptiveInspectionEstimateNameLengthRule(32),
            AdaptiveInspectionEstimateRequiredCodeRule("adaptive inspection estimate needs an entry"),
            AdaptiveInspectionEstimateAttemptsRangeRule(64, 255),
            AdaptiveInspectionEstimateRequiredSourceRule("adaptive inspection estimate needs a value"),
            AdaptiveInspectionEstimateAmountRangeRule(3.1, 3297.3),
        ),
    )
}
