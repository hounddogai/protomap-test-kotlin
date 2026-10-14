package com.acme.transport.fleetops.inspection.reservation

/** A rule a lenient inspection segment must pass. Check returns its problems, or none. */
interface LenientInspectionSegmentRule {
    fun check(item: LenientInspectionSegment): MutableList<String>
}

data class LenientInspectionSegmentLabelLengthRule(var maxLength: Int) : LenientInspectionSegmentRule {
    override fun check(item: LenientInspectionSegment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientInspectionSegmentRequiredLocaleRule(var message: String) : LenientInspectionSegmentRule {
    override fun check(item: LenientInspectionSegment): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientInspectionSegmentCodePrefixRule(var prefix: String) : LenientInspectionSegmentRule {
    override fun check(item: LenientInspectionSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientInspectionSegmentScoreRangeRule(
    var min: Double,
    var max: Double,
) : LenientInspectionSegmentRule {
    override fun check(item: LenientInspectionSegment): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientInspectionSegmentRouteCodeLengthRule(var maxLength: Int) : LenientInspectionSegmentRule {
    override fun check(item: LenientInspectionSegment): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientInspectionSegmentSequenceRangeRule(
    var min: Int,
    var max: Int,
) : LenientInspectionSegmentRule {
    override fun check(item: LenientInspectionSegment): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient inspection segment. */
data class LenientInspectionSegmentValidator(var rules: MutableList<LenientInspectionSegmentRule>) {
    fun validate(item: LenientInspectionSegment): MutableList<String> {
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

    fun requireValid(item: LenientInspectionSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientInspectionSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientInspectionSegmentValidator(): LenientInspectionSegmentValidator {
    return LenientInspectionSegmentValidator(
        mutableListOf<LenientInspectionSegmentRule>(
            LenientInspectionSegmentLabelLengthRule(120),
            LenientInspectionSegmentRequiredLocaleRule("lenient inspection segment needs a value"),
            LenientInspectionSegmentCodePrefixRule("OPS"),
            LenientInspectionSegmentScoreRangeRule(8.3, 3545.2),
            LenientInspectionSegmentRouteCodeLengthRule(1000),
            LenientInspectionSegmentSequenceRangeRule(16, 32),
        ),
    )
}
