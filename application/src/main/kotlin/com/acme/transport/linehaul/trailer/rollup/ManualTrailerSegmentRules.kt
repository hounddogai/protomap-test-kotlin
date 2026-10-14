package com.acme.transport.linehaul.trailer.rollup

import com.acme.transport.kit.text.normalize

/** A rule a manual trailer segment must pass. Check returns its problems, or none. */
interface ManualTrailerSegmentRule {
    fun check(item: ManualTrailerSegment): MutableList<String>
}

data class ManualTrailerSegmentCodeLengthRule(var maxLength: Int) : ManualTrailerSegmentRule {
    override fun check(item: ManualTrailerSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerSegmentLabelLengthRule(var maxLength: Int) : ManualTrailerSegmentRule {
    override fun check(item: ManualTrailerSegment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerSegmentAllowedTrailerIdRule(
    var allowed: MutableList<String>,
) : ManualTrailerSegmentRule {
    override fun check(item: ManualTrailerSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerSegmentRequiredCodeRule(var message: String) : ManualTrailerSegmentRule {
    override fun check(item: ManualTrailerSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerSegmentThresholdRangeRule(var min: Double, var max: Double) : ManualTrailerSegmentRule {
    override fun check(item: ManualTrailerSegment): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerSegmentRequiredLabelRule(var message: String) : ManualTrailerSegmentRule {
    override fun check(item: ManualTrailerSegment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual trailer segment. */
data class ManualTrailerSegmentValidator(var rules: MutableList<ManualTrailerSegmentRule>) {
    fun validate(item: ManualTrailerSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualTrailerSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTrailerSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTrailerSegmentValidator(): ManualTrailerSegmentValidator {
    return ManualTrailerSegmentValidator(
        mutableListOf<ManualTrailerSegmentRule>(
            ManualTrailerSegmentCodeLengthRule(120),
            ManualTrailerSegmentLabelLengthRule(16),
            ManualTrailerSegmentAllowedTrailerIdRule(mutableListOf<String>("pending", "batched")),
            ManualTrailerSegmentRequiredCodeRule("manual trailer segment needs a value"),
            ManualTrailerSegmentThresholdRangeRule(4.3, 2185.5),
            ManualTrailerSegmentRequiredLabelRule("manual trailer segment needs this field"),
        ),
    )
}
