package com.acme.transport.safety.scorecard.segment

/** A rule a capped scorecard segment must pass. Check returns its problems, or none. */
interface CappedScorecardSegmentRule {
    fun check(item: CappedScorecardSegment): MutableList<String>
}

data class CappedScorecardSegmentRequiredTitleRule(var message: String) : CappedScorecardSegmentRule {
    override fun check(item: CappedScorecardSegment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedScorecardSegmentRequiredSourceRule(var message: String) : CappedScorecardSegmentRule {
    override fun check(item: CappedScorecardSegment): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedScorecardSegmentTitleLengthRule(var maxLength: Int) : CappedScorecardSegmentRule {
    override fun check(item: CappedScorecardSegment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedScorecardSegmentActiveGuardRule(var limit: Int) : CappedScorecardSegmentRule {
    override fun check(item: CappedScorecardSegment): MutableList<String> {
        if (item.active && item.priority > limit) {
            return mutableListOf<String>("priority ${item.priority} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped scorecard segment. */
data class CappedScorecardSegmentValidator(var rules: MutableList<CappedScorecardSegmentRule>) {
    fun validate(item: CappedScorecardSegment): MutableList<String> {
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

    fun requireValid(item: CappedScorecardSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedScorecardSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedScorecardSegmentValidator(): CappedScorecardSegmentValidator {
    return CappedScorecardSegmentValidator(
        mutableListOf<CappedScorecardSegmentRule>(
            CappedScorecardSegmentRequiredTitleRule("capped scorecard segment needs a value"),
            CappedScorecardSegmentRequiredSourceRule("capped scorecard segment needs a value"),
            CappedScorecardSegmentTitleLengthRule(80),
            CappedScorecardSegmentActiveGuardRule(80),
        ),
    )
}
