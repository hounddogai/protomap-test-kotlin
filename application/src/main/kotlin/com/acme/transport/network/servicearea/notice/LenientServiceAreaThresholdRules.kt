package com.acme.transport.network.servicearea.notice

/** A rule a lenient service area threshold must pass. Check returns its problems, or none. */
interface LenientServiceAreaThresholdRule {
    fun check(item: LenientServiceAreaThreshold): MutableList<String>
}

data class LenientServiceAreaThresholdRequiredCurrencyRule(
    var message: String,
) : LenientServiceAreaThresholdRule {
    override fun check(item: LenientServiceAreaThreshold): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientServiceAreaThresholdRevisionRangeRule(
    var min: Int,
    var max: Int,
) : LenientServiceAreaThresholdRule {
    override fun check(item: LenientServiceAreaThreshold): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientServiceAreaThresholdRequiredRegionRule(
    var message: String,
) : LenientServiceAreaThresholdRule {
    override fun check(item: LenientServiceAreaThreshold): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientServiceAreaThresholdCurrencyLengthRule(var maxLength: Int) : LenientServiceAreaThresholdRule {
    override fun check(item: LenientServiceAreaThreshold): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientServiceAreaThresholdOdometerRangeRule(
    var min: Int,
    var max: Int,
) : LenientServiceAreaThresholdRule {
    override fun check(item: LenientServiceAreaThreshold): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient service area threshold. */
data class LenientServiceAreaThresholdValidator(var rules: MutableList<LenientServiceAreaThresholdRule>) {
    fun validate(item: LenientServiceAreaThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientServiceAreaThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientServiceAreaThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientServiceAreaThresholdValidator(): LenientServiceAreaThresholdValidator {
    return LenientServiceAreaThresholdValidator(
        mutableListOf<LenientServiceAreaThresholdRule>(
            LenientServiceAreaThresholdRequiredCurrencyRule("lenient service area threshold needs a value"),
            LenientServiceAreaThresholdRevisionRangeRule(3600, 3600),
            LenientServiceAreaThresholdRequiredRegionRule("lenient service area threshold needs a value"),
            LenientServiceAreaThresholdCurrencyLengthRule(32),
            LenientServiceAreaThresholdOdometerRangeRule(3600, 86400),
        ),
    )
}
