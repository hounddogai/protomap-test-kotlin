package com.acme.transport.safety.coaching.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a global coaching adjustment must pass. Check returns its problems, or none. */
interface GlobalCoachingAdjustmentRule {
    fun check(item: GlobalCoachingAdjustment): MutableList<String>
}

data class GlobalCoachingAdjustmentCodeLengthRule(var maxLength: Int) : GlobalCoachingAdjustmentRule {
    override fun check(item: GlobalCoachingAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCoachingAdjustmentAllowedCodeRule(
    var allowed: MutableList<String>,
) : GlobalCoachingAdjustmentRule {
    override fun check(item: GlobalCoachingAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCoachingAdjustmentCodePrefixRule(var prefix: String) : GlobalCoachingAdjustmentRule {
    override fun check(item: GlobalCoachingAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCoachingAdjustmentRateRangeRule(
    var min: Double,
    var max: Double,
) : GlobalCoachingAdjustmentRule {
    override fun check(item: GlobalCoachingAdjustment): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalCoachingAdjustmentFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : GlobalCoachingAdjustmentRule {
    override fun check(item: GlobalCoachingAdjustment): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global coaching adjustment. */
data class GlobalCoachingAdjustmentValidator(var rules: MutableList<GlobalCoachingAdjustmentRule>) {
    fun validate(item: GlobalCoachingAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalCoachingAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalCoachingAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalCoachingAdjustmentValidator(): GlobalCoachingAdjustmentValidator {
    return GlobalCoachingAdjustmentValidator(
        mutableListOf<GlobalCoachingAdjustmentRule>(
            GlobalCoachingAdjustmentCodeLengthRule(1000),
            GlobalCoachingAdjustmentAllowedCodeRule(mutableListOf<String>("lenient", "regional", "global")),
            GlobalCoachingAdjustmentCodePrefixRule("OPS"),
            GlobalCoachingAdjustmentRateRangeRule(9.2, 3707.5),
            GlobalCoachingAdjustmentFloorValueRangeRule(2.3, 1710.8),
        ),
    )
}
