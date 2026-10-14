package com.acme.transport.safety.coaching.bundle

import com.acme.transport.kit.text.normalize

/** A rule a coaching summary must pass. Check returns its problems, or none. */
interface CoachingSummaryRule {
    fun check(item: CoachingSummary): MutableList<String>
}

data class CoachingSummaryTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : CoachingSummaryRule {
    override fun check(item: CoachingSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class CoachingSummaryCeilingRangeRule(var min: Double, var max: Double) : CoachingSummaryRule {
    override fun check(item: CoachingSummary): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CoachingSummaryDepotLengthRule(var maxLength: Int) : CoachingSummaryRule {
    override fun check(item: CoachingSummary): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingSummaryAllowedCodeRule(var allowed: MutableList<String>) : CoachingSummaryRule {
    override fun check(item: CoachingSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching summary. */
data class CoachingSummaryValidator(var rules: MutableList<CoachingSummaryRule>) {
    fun validate(item: CoachingSummary): MutableList<String> {
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

    fun requireValid(item: CoachingSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingSummaryValidator(): CoachingSummaryValidator {
    return CoachingSummaryValidator(
        mutableListOf<CoachingSummaryRule>(
            CoachingSummaryTagsLimitRule(86400, mutableListOf<String>("batched", "rolling")),
            CoachingSummaryCeilingRangeRule(1.2, 1864.9),
            CoachingSummaryDepotLengthRule(120),
            CoachingSummaryAllowedCodeRule(mutableListOf<String>("weighted", "manual", "lenient")),
        ),
    )
}
