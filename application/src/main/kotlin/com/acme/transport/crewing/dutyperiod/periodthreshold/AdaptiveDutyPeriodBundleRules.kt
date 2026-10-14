package com.acme.transport.crewing.dutyperiod.periodthreshold

/** A rule a adaptive duty period bundle must pass. Check returns its problems, or none. */
interface AdaptiveDutyPeriodBundleRule {
    fun check(item: AdaptiveDutyPeriodBundle): MutableList<String>
}

data class AdaptiveDutyPeriodBundleRequiredRouteCodeRule(var message: String) : AdaptiveDutyPeriodBundleRule {
    override fun check(item: AdaptiveDutyPeriodBundle): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDutyPeriodBundleVisibleGuardRule(var limit: Double) : AdaptiveDutyPeriodBundleRule {
    override fun check(item: AdaptiveDutyPeriodBundle): MutableList<String> {
        if (item.visible && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDutyPeriodBundleRouteCodeLengthRule(var maxLength: Int) : AdaptiveDutyPeriodBundleRule {
    override fun check(item: AdaptiveDutyPeriodBundle): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDutyPeriodBundleNameLengthRule(var maxLength: Int) : AdaptiveDutyPeriodBundleRule {
    override fun check(item: AdaptiveDutyPeriodBundle): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDutyPeriodBundleAmountRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveDutyPeriodBundleRule {
    override fun check(item: AdaptiveDutyPeriodBundle): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a adaptive duty period bundle. */
data class AdaptiveDutyPeriodBundleValidator(var rules: MutableList<AdaptiveDutyPeriodBundleRule>) {
    fun validate(item: AdaptiveDutyPeriodBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveDutyPeriodBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveDutyPeriodBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveDutyPeriodBundleValidator(): AdaptiveDutyPeriodBundleValidator {
    return AdaptiveDutyPeriodBundleValidator(
        mutableListOf<AdaptiveDutyPeriodBundleRule>(
            AdaptiveDutyPeriodBundleRequiredRouteCodeRule("adaptive duty period bundle needs a value"),
            AdaptiveDutyPeriodBundleVisibleGuardRule(6.8),
            AdaptiveDutyPeriodBundleRouteCodeLengthRule(16),
            AdaptiveDutyPeriodBundleNameLengthRule(255),
            AdaptiveDutyPeriodBundleAmountRangeRule(4.6, 3909.0),
        ),
    )
}
