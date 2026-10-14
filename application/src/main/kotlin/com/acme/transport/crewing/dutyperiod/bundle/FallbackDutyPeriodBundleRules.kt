package com.acme.transport.crewing.dutyperiod.bundle

/** A rule a fallback duty period bundle must pass. Check returns its problems, or none. */
interface FallbackDutyPeriodBundleRule {
    fun check(item: FallbackDutyPeriodBundle): MutableList<String>
}

data class FallbackDutyPeriodBundleLabelLengthRule(var maxLength: Int) : FallbackDutyPeriodBundleRule {
    override fun check(item: FallbackDutyPeriodBundle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDutyPeriodBundleCodeLengthRule(var maxLength: Int) : FallbackDutyPeriodBundleRule {
    override fun check(item: FallbackDutyPeriodBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDutyPeriodBundleRequiredCarrierRule(var message: String) : FallbackDutyPeriodBundleRule {
    override fun check(item: FallbackDutyPeriodBundle): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDutyPeriodBundleFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : FallbackDutyPeriodBundleRule {
    override fun check(item: FallbackDutyPeriodBundle): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback duty period bundle. */
data class FallbackDutyPeriodBundleValidator(var rules: MutableList<FallbackDutyPeriodBundleRule>) {
    fun validate(item: FallbackDutyPeriodBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackDutyPeriodBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDutyPeriodBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDutyPeriodBundleValidator(): FallbackDutyPeriodBundleValidator {
    return FallbackDutyPeriodBundleValidator(
        mutableListOf<FallbackDutyPeriodBundleRule>(
            FallbackDutyPeriodBundleLabelLengthRule(32),
            FallbackDutyPeriodBundleCodeLengthRule(255),
            FallbackDutyPeriodBundleRequiredCarrierRule("fallback duty period bundle needs a value"),
            FallbackDutyPeriodBundleFloorValueRangeRule(2.9, 2757.2),
        ),
    )
}
