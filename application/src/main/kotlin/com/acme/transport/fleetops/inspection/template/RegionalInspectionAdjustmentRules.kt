package com.acme.transport.fleetops.inspection.template

import com.acme.transport.kit.text.normalize

/** A rule a regional inspection adjustment must pass. Check returns its problems, or none. */
interface RegionalInspectionAdjustmentRule {
    fun check(item: RegionalInspectionAdjustment): MutableList<String>
}

data class RegionalInspectionAdjustmentTrailerIdLengthRule(
    var maxLength: Int,
) : RegionalInspectionAdjustmentRule {
    override fun check(item: RegionalInspectionAdjustment): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalInspectionAdjustmentCodePrefixRule(var prefix: String) : RegionalInspectionAdjustmentRule {
    override fun check(item: RegionalInspectionAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalInspectionAdjustmentRequiredNameRule(
    var message: String,
) : RegionalInspectionAdjustmentRule {
    override fun check(item: RegionalInspectionAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalInspectionAdjustmentRatioRangeRule(
    var min: Double,
    var max: Double,
) : RegionalInspectionAdjustmentRule {
    override fun check(item: RegionalInspectionAdjustment): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalInspectionAdjustmentAllowedTrailerIdRule(
    var allowed: MutableList<String>,
) : RegionalInspectionAdjustmentRule {
    override fun check(item: RegionalInspectionAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is stale")
        }
        return mutableListOf<String>()
    }
}

data class RegionalInspectionAdjustmentLaneLengthRule(var maxLength: Int) : RegionalInspectionAdjustmentRule {
    override fun check(item: RegionalInspectionAdjustment): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional inspection adjustment. */
data class RegionalInspectionAdjustmentValidator(var rules: MutableList<RegionalInspectionAdjustmentRule>) {
    fun validate(item: RegionalInspectionAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalInspectionAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalInspectionAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalInspectionAdjustmentValidator(): RegionalInspectionAdjustmentValidator {
    return RegionalInspectionAdjustmentValidator(
        mutableListOf<RegionalInspectionAdjustmentRule>(
            RegionalInspectionAdjustmentTrailerIdLengthRule(86400),
            RegionalInspectionAdjustmentCodePrefixRule("X"),
            RegionalInspectionAdjustmentRequiredNameRule("regional inspection adjustment needs a value"),
            RegionalInspectionAdjustmentRatioRangeRule(1.8, 3435.7),
            RegionalInspectionAdjustmentAllowedTrailerIdRule(
                mutableListOf<String>("tiered", "default", "legacy"),
            ),
            RegionalInspectionAdjustmentLaneLengthRule(16),
        ),
    )
}
