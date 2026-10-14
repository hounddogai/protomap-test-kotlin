package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.kit.text.normalize

/** A rule a capped trailer group must pass. Check returns its problems, or none. */
interface CappedTrailerGroupRule {
    fun check(item: CappedTrailerGroup): MutableList<String>
}

data class CappedTrailerGroupAllowedTitleRule(var allowed: MutableList<String>) : CappedTrailerGroupRule {
    override fun check(item: CappedTrailerGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerGroupNameLengthRule(var maxLength: Int) : CappedTrailerGroupRule {
    override fun check(item: CappedTrailerGroup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerGroupGraceDaysRangeRule(var min: Int, var max: Int) : CappedTrailerGroupRule {
    override fun check(item: CappedTrailerGroup): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped trailer group. */
data class CappedTrailerGroupValidator(var rules: MutableList<CappedTrailerGroupRule>) {
    fun validate(item: CappedTrailerGroup): MutableList<String> {
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

    fun requireValid(item: CappedTrailerGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedTrailerGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedTrailerGroupValidator(): CappedTrailerGroupValidator {
    return CappedTrailerGroupValidator(
        mutableListOf<CappedTrailerGroupRule>(
            CappedTrailerGroupAllowedTitleRule(mutableListOf<String>("primary")),
            CappedTrailerGroupNameLengthRule(64),
            CappedTrailerGroupGraceDaysRangeRule(32, 120),
        ),
    )
}
