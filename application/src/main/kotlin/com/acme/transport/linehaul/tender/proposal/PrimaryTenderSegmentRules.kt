package com.acme.transport.linehaul.tender.proposal

import com.acme.transport.kit.text.normalize

/** A rule a primary tender segment must pass. Check returns its problems, or none. */
interface PrimaryTenderSegmentRule {
    fun check(item: PrimaryTenderSegment): MutableList<String>
}

data class PrimaryTenderSegmentDiscountRangeRule(var min: Double, var max: Double) : PrimaryTenderSegmentRule {
    override fun check(item: PrimaryTenderSegment): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTenderSegmentDurationMinutesRangeRule(var min: Int, var max: Int) : PrimaryTenderSegmentRule {
    override fun check(item: PrimaryTenderSegment): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class PrimaryTenderSegmentAllowedTitleRule(var allowed: MutableList<String>) : PrimaryTenderSegmentRule {
    override fun check(item: PrimaryTenderSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTenderSegmentDepotLengthRule(var maxLength: Int) : PrimaryTenderSegmentRule {
    override fun check(item: PrimaryTenderSegment): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTenderSegmentTitleLengthRule(var maxLength: Int) : PrimaryTenderSegmentRule {
    override fun check(item: PrimaryTenderSegment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary tender segment. */
data class PrimaryTenderSegmentValidator(var rules: MutableList<PrimaryTenderSegmentRule>) {
    fun validate(item: PrimaryTenderSegment): MutableList<String> {
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

    fun requireValid(item: PrimaryTenderSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTenderSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTenderSegmentValidator(): PrimaryTenderSegmentValidator {
    return PrimaryTenderSegmentValidator(
        mutableListOf<PrimaryTenderSegmentRule>(
            PrimaryTenderSegmentDiscountRangeRule(7.4, 737.6),
            PrimaryTenderSegmentDurationMinutesRangeRule(120, 86400),
            PrimaryTenderSegmentAllowedTitleRule(mutableListOf<String>()),
            PrimaryTenderSegmentDepotLengthRule(86400),
            PrimaryTenderSegmentTitleLengthRule(80),
        ),
    )
}
