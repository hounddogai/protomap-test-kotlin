package com.acme.transport.customs.inspection.plan

/** A rule a adaptive inspection threshold must pass. Check returns its problems, or none. */
interface AdaptiveInspectionThresholdRule {
    fun check(item: AdaptiveInspectionThreshold): MutableList<String>
}

data class AdaptiveInspectionThresholdGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveInspectionThresholdRule {
    override fun check(item: AdaptiveInspectionThreshold): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionThresholdOdometerRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveInspectionThresholdRule {
    override fun check(item: AdaptiveInspectionThreshold): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class AdaptiveInspectionThresholdRequiredCodeRule(var message: String) : AdaptiveInspectionThresholdRule {
    override fun check(item: AdaptiveInspectionThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionThresholdRetriesRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveInspectionThresholdRule {
    override fun check(item: AdaptiveInspectionThreshold): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionThresholdCodeLengthRule(var maxLength: Int) : AdaptiveInspectionThresholdRule {
    override fun check(item: AdaptiveInspectionThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive inspection threshold. */
data class AdaptiveInspectionThresholdValidator(var rules: MutableList<AdaptiveInspectionThresholdRule>) {
    fun validate(item: AdaptiveInspectionThreshold): MutableList<String> {
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

    fun requireValid(item: AdaptiveInspectionThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveInspectionThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveInspectionThresholdValidator(): AdaptiveInspectionThresholdValidator {
    return AdaptiveInspectionThresholdValidator(
        mutableListOf<AdaptiveInspectionThresholdRule>(
            AdaptiveInspectionThresholdGraceDaysRangeRule(120, 3600),
            AdaptiveInspectionThresholdOdometerRangeRule(32, 32),
            AdaptiveInspectionThresholdRequiredCodeRule("adaptive inspection threshold needs a value"),
            AdaptiveInspectionThresholdRetriesRangeRule(16, 255),
            AdaptiveInspectionThresholdCodeLengthRule(86400),
        ),
    )
}
