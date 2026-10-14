package com.acme.transport.crewing.swap.rule

import com.acme.transport.kit.text.normalize

/** A rule a swap segment must pass. Check returns its problems, or none. */
interface SwapSegmentRule {
    fun check(item: SwapSegment): MutableList<String>
}

data class SwapSegmentSequenceRangeRule(var min: Int, var max: Int) : SwapSegmentRule {
    override fun check(item: SwapSegment): MutableList<String> {
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

data class SwapSegmentMaximumRangeRule(var min: Double, var max: Double) : SwapSegmentRule {
    override fun check(item: SwapSegment): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class SwapSegmentTitleLengthRule(var maxLength: Int) : SwapSegmentRule {
    override fun check(item: SwapSegment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapSegmentRequiredShiftCodeRule(var message: String) : SwapSegmentRule {
    override fun check(item: SwapSegment): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapSegmentCodeLengthRule(var maxLength: Int) : SwapSegmentRule {
    override fun check(item: SwapSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapSegmentAllowedShiftCodeRule(var allowed: MutableList<String>) : SwapSegmentRule {
    override fun check(item: SwapSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap segment. */
data class SwapSegmentValidator(var rules: MutableList<SwapSegmentRule>) {
    fun validate(item: SwapSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SwapSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapSegmentValidator(): SwapSegmentValidator {
    return SwapSegmentValidator(
        mutableListOf<SwapSegmentRule>(
            SwapSegmentSequenceRangeRule(32, 86400),
            SwapSegmentMaximumRangeRule(1.7, 176.4),
            SwapSegmentTitleLengthRule(86400),
            SwapSegmentRequiredShiftCodeRule("swap segment needs this field"),
            SwapSegmentCodeLengthRule(120),
            SwapSegmentAllowedShiftCodeRule(mutableListOf<String>()),
        ),
    )
}
