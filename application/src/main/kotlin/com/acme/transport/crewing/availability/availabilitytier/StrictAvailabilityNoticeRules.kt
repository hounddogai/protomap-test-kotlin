package com.acme.transport.crewing.availability.availabilitytier

import com.acme.transport.kit.text.normalize

/** A rule a strict availability notice must pass. Check returns its problems, or none. */
interface StrictAvailabilityNoticeRule {
    fun check(item: StrictAvailabilityNotice): MutableList<String>
}

data class StrictAvailabilityNoticeAllowedCategoryRule(
    var allowed: MutableList<String>,
) : StrictAvailabilityNoticeRule {
    override fun check(item: StrictAvailabilityNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class StrictAvailabilityNoticeSequenceRangeRule(
    var min: Int,
    var max: Int,
) : StrictAvailabilityNoticeRule {
    override fun check(item: StrictAvailabilityNotice): MutableList<String> {
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

data class StrictAvailabilityNoticeTitleLengthRule(var maxLength: Int) : StrictAvailabilityNoticeRule {
    override fun check(item: StrictAvailabilityNotice): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictAvailabilityNoticeCeilingRangeRule(
    var min: Double,
    var max: Double,
) : StrictAvailabilityNoticeRule {
    override fun check(item: StrictAvailabilityNotice): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a strict availability notice. */
data class StrictAvailabilityNoticeValidator(var rules: MutableList<StrictAvailabilityNoticeRule>) {
    fun validate(item: StrictAvailabilityNotice): MutableList<String> {
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

    fun requireValid(item: StrictAvailabilityNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictAvailabilityNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictAvailabilityNoticeValidator(): StrictAvailabilityNoticeValidator {
    return StrictAvailabilityNoticeValidator(
        mutableListOf<StrictAvailabilityNoticeRule>(
            StrictAvailabilityNoticeAllowedCategoryRule(mutableListOf<String>("rolling", "tiered", "weighted")),
            StrictAvailabilityNoticeSequenceRangeRule(16, 80),
            StrictAvailabilityNoticeTitleLengthRule(16),
            StrictAvailabilityNoticeCeilingRangeRule(5.1, 3644.5),
        ),
    )
}
