package com.acme.transport.fleetops.inspection.adjustment

/** A rule a adaptive inspection policy must pass. Check returns its problems, or none. */
interface AdaptiveInspectionPolicyRule {
    fun check(item: AdaptiveInspectionPolicy): MutableList<String>
}

data class AdaptiveInspectionPolicyLaneLengthRule(var maxLength: Int) : AdaptiveInspectionPolicyRule {
    override fun check(item: AdaptiveInspectionPolicy): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionPolicyCodeLengthRule(var maxLength: Int) : AdaptiveInspectionPolicyRule {
    override fun check(item: AdaptiveInspectionPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionPolicyOdometerRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveInspectionPolicyRule {
    override fun check(item: AdaptiveInspectionPolicy): MutableList<String> {
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

data class AdaptiveInspectionPolicyNameLengthRule(var maxLength: Int) : AdaptiveInspectionPolicyRule {
    override fun check(item: AdaptiveInspectionPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionPolicyMarginRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveInspectionPolicyRule {
    override fun check(item: AdaptiveInspectionPolicy): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive inspection policy. */
data class AdaptiveInspectionPolicyValidator(var rules: MutableList<AdaptiveInspectionPolicyRule>) {
    fun validate(item: AdaptiveInspectionPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveInspectionPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveInspectionPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveInspectionPolicyValidator(): AdaptiveInspectionPolicyValidator {
    return AdaptiveInspectionPolicyValidator(
        mutableListOf<AdaptiveInspectionPolicyRule>(
            AdaptiveInspectionPolicyLaneLengthRule(255),
            AdaptiveInspectionPolicyCodeLengthRule(3600),
            AdaptiveInspectionPolicyOdometerRangeRule(16, 1000),
            AdaptiveInspectionPolicyNameLengthRule(16),
            AdaptiveInspectionPolicyMarginRangeRule(7.2, 2703.8),
        ),
    )
}
