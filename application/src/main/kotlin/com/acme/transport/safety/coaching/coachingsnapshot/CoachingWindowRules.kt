package com.acme.transport.safety.coaching.coachingsnapshot

/** A rule a coaching window must pass. Check returns its problems, or none. */
interface CoachingWindowRule {
    fun check(item: CoachingWindow): MutableList<String>
}

data class CoachingWindowRequiredCodeRule(var message: String) : CoachingWindowRule {
    override fun check(item: CoachingWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingWindowTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : CoachingWindowRule {
    override fun check(item: CoachingWindow): MutableList<String> {
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

data class CoachingWindowRequiredLabelRule(var message: String) : CoachingWindowRule {
    override fun check(item: CoachingWindow): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching window. */
data class CoachingWindowValidator(var rules: MutableList<CoachingWindowRule>) {
    fun validate(item: CoachingWindow): MutableList<String> {
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

    fun requireValid(item: CoachingWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingWindowValidator(): CoachingWindowValidator {
    return CoachingWindowValidator(
        mutableListOf<CoachingWindowRule>(
            CoachingWindowRequiredCodeRule("coaching window needs this field"),
            CoachingWindowTagsLimitRule(64, mutableListOf<String>("rolling", "scheduled")),
            CoachingWindowRequiredLabelRule("coaching window needs this field"),
        ),
    )
}
