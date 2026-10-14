package com.acme.transport.terminal.traileryard.yardentry

import com.acme.transport.kit.text.normalize

/** A rule a lenient trailer yard segment must pass. Check returns its problems, or none. */
interface LenientTrailerYardSegmentRule {
    fun check(item: LenientTrailerYardSegment): MutableList<String>
}

data class LenientTrailerYardSegmentDriverIdLengthRule(var maxLength: Int) : LenientTrailerYardSegmentRule {
    override fun check(item: LenientTrailerYardSegment): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientTrailerYardSegmentAllowedCodeRule(
    var allowed: MutableList<String>,
) : LenientTrailerYardSegmentRule {
    override fun check(item: LenientTrailerYardSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class LenientTrailerYardSegmentLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : LenientTrailerYardSegmentRule {
    override fun check(item: LenientTrailerYardSegment): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient trailer yard segment. */
data class LenientTrailerYardSegmentValidator(var rules: MutableList<LenientTrailerYardSegmentRule>) {
    fun validate(item: LenientTrailerYardSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientTrailerYardSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientTrailerYardSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientTrailerYardSegmentValidator(): LenientTrailerYardSegmentValidator {
    return LenientTrailerYardSegmentValidator(
        mutableListOf<LenientTrailerYardSegmentRule>(
            LenientTrailerYardSegmentDriverIdLengthRule(64),
            LenientTrailerYardSegmentAllowedCodeRule(mutableListOf<String>("seasonal", "seasonal")),
            LenientTrailerYardSegmentLoadKgRangeRule(2.8, 2313.6),
        ),
    )
}
