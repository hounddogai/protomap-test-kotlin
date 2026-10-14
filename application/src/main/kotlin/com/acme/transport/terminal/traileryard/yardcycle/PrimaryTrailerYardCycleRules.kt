package com.acme.transport.terminal.traileryard.yardcycle

import com.acme.transport.kit.text.normalize

/** A rule a primary trailer yard cycle must pass. Check returns its problems, or none. */
interface PrimaryTrailerYardCycleRule {
    fun check(item: PrimaryTrailerYardCycle): MutableList<String>
}

data class PrimaryTrailerYardCycleSegmentLengthRule(var maxLength: Int) : PrimaryTrailerYardCycleRule {
    override fun check(item: PrimaryTrailerYardCycle): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTrailerYardCycleEndsAtRangeRule(var min: Int, var max: Int) : PrimaryTrailerYardCycleRule {
    override fun check(item: PrimaryTrailerYardCycle): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class PrimaryTrailerYardCycleAllowedSegmentRule(
    var allowed: MutableList<String>,
) : PrimaryTrailerYardCycleRule {
    override fun check(item: PrimaryTrailerYardCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTrailerYardCycleAmountRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryTrailerYardCycleRule {
    override fun check(item: PrimaryTrailerYardCycle): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class PrimaryTrailerYardCyclePinnedGuardRule(var limit: Int) : PrimaryTrailerYardCycleRule {
    override fun check(item: PrimaryTrailerYardCycle): MutableList<String> {
        if (item.pinned && item.endsAt > limit) {
            return mutableListOf<String>("ends at ${item.endsAt} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary trailer yard cycle. */
data class PrimaryTrailerYardCycleValidator(var rules: MutableList<PrimaryTrailerYardCycleRule>) {
    fun validate(item: PrimaryTrailerYardCycle): MutableList<String> {
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

    fun requireValid(item: PrimaryTrailerYardCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTrailerYardCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTrailerYardCycleValidator(): PrimaryTrailerYardCycleValidator {
    return PrimaryTrailerYardCycleValidator(
        mutableListOf<PrimaryTrailerYardCycleRule>(
            PrimaryTrailerYardCycleSegmentLengthRule(16),
            PrimaryTrailerYardCycleEndsAtRangeRule(80, 3600),
            PrimaryTrailerYardCycleAllowedSegmentRule(mutableListOf<String>()),
            PrimaryTrailerYardCycleAmountRangeRule(0.0, 3475.9),
            PrimaryTrailerYardCyclePinnedGuardRule(16),
        ),
    )
}
