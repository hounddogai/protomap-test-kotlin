package com.acme.transport.fleetops.utilization.tieredutilizationplan

import com.acme.transport.kit.text.normalize

/** A rule a legacy utilization segment must pass. Check returns its problems, or none. */
interface LegacyUtilizationSegmentRule {
    fun check(item: LegacyUtilizationSegment): MutableList<String>
}

data class LegacyUtilizationSegmentDiscountRangeRule(
    var min: Double,
    var max: Double,
) : LegacyUtilizationSegmentRule {
    override fun check(item: LegacyUtilizationSegment): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyUtilizationSegmentNameLengthRule(var maxLength: Int) : LegacyUtilizationSegmentRule {
    override fun check(item: LegacyUtilizationSegment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyUtilizationSegmentQuantityRangeRule(
    var min: Int,
    var max: Int,
) : LegacyUtilizationSegmentRule {
    override fun check(item: LegacyUtilizationSegment): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyUtilizationSegmentDescriptionLengthRule(var maxLength: Int) : LegacyUtilizationSegmentRule {
    override fun check(item: LegacyUtilizationSegment): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyUtilizationSegmentLimitRangeRule(var min: Int, var max: Int) : LegacyUtilizationSegmentRule {
    override fun check(item: LegacyUtilizationSegment): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class LegacyUtilizationSegmentAllowedCodeRule(
    var allowed: MutableList<String>,
) : LegacyUtilizationSegmentRule {
    override fun check(item: LegacyUtilizationSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy utilization segment. */
data class LegacyUtilizationSegmentValidator(var rules: MutableList<LegacyUtilizationSegmentRule>) {
    fun validate(item: LegacyUtilizationSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyUtilizationSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyUtilizationSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyUtilizationSegmentValidator(): LegacyUtilizationSegmentValidator {
    return LegacyUtilizationSegmentValidator(
        mutableListOf<LegacyUtilizationSegmentRule>(
            LegacyUtilizationSegmentDiscountRangeRule(1.1, 3729.6),
            LegacyUtilizationSegmentNameLengthRule(86400),
            LegacyUtilizationSegmentQuantityRangeRule(16, 1000),
            LegacyUtilizationSegmentDescriptionLengthRule(32),
            LegacyUtilizationSegmentLimitRangeRule(1000, 3600),
            LegacyUtilizationSegmentAllowedCodeRule(mutableListOf<String>("adaptive")),
        ),
    )
}
