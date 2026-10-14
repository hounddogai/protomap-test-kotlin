package com.acme.transport.linehaul.trailer.threshold

/** A rule a trailer segment must pass. Check returns its problems, or none. */
interface TrailerSegmentRule {
    fun check(item: TrailerSegment): MutableList<String>
}

data class TrailerSegmentTitleLengthRule(var maxLength: Int) : TrailerSegmentRule {
    override fun check(item: TrailerSegment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerSegmentRequiredLabelRule(var message: String) : TrailerSegmentRule {
    override fun check(item: TrailerSegment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerSegmentRequiredCodeRule(var message: String) : TrailerSegmentRule {
    override fun check(item: TrailerSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerSegmentLabelLengthRule(var maxLength: Int) : TrailerSegmentRule {
    override fun check(item: TrailerSegment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerSegmentFloorValueRangeRule(var min: Double, var max: Double) : TrailerSegmentRule {
    override fun check(item: TrailerSegment): MutableList<String> {
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

data class TrailerSegmentStopCountRangeRule(var min: Int, var max: Int) : TrailerSegmentRule {
    override fun check(item: TrailerSegment): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer segment. */
data class TrailerSegmentValidator(var rules: MutableList<TrailerSegmentRule>) {
    fun validate(item: TrailerSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerSegmentValidator(): TrailerSegmentValidator {
    return TrailerSegmentValidator(
        mutableListOf<TrailerSegmentRule>(
            TrailerSegmentTitleLengthRule(1000),
            TrailerSegmentRequiredLabelRule("trailer segment needs this field"),
            TrailerSegmentRequiredCodeRule("trailer segment needs an entry"),
            TrailerSegmentLabelLengthRule(32),
            TrailerSegmentFloorValueRangeRule(1.0, 84.6),
            TrailerSegmentStopCountRangeRule(80, 86400),
        ),
    )
}
