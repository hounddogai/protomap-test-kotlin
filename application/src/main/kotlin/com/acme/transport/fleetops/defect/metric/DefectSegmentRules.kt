package com.acme.transport.fleetops.defect.metric

import com.acme.transport.kit.text.normalize

/** A rule a defect segment must pass. Check returns its problems, or none. */
interface DefectSegmentRule {
    fun check(item: DefectSegment): MutableList<String>
}

data class DefectSegmentAllowedShiftCodeRule(var allowed: MutableList<String>) : DefectSegmentRule {
    override fun check(item: DefectSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is missing")
        }
        return mutableListOf<String>()
    }
}

data class DefectSegmentShiftCodeLengthRule(var maxLength: Int) : DefectSegmentRule {
    override fun check(item: DefectSegment): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectSegmentDistanceKmRangeRule(var min: Double, var max: Double) : DefectSegmentRule {
    override fun check(item: DefectSegment): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a defect segment. */
data class DefectSegmentValidator(var rules: MutableList<DefectSegmentRule>) {
    fun validate(item: DefectSegment): MutableList<String> {
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

    fun requireValid(item: DefectSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectSegmentValidator(): DefectSegmentValidator {
    return DefectSegmentValidator(
        mutableListOf<DefectSegmentRule>(
            DefectSegmentAllowedShiftCodeRule(mutableListOf<String>("adaptive", "legacy")),
            DefectSegmentShiftCodeLengthRule(255),
            DefectSegmentDistanceKmRangeRule(3.8, 1743.8),
        ),
    )
}
