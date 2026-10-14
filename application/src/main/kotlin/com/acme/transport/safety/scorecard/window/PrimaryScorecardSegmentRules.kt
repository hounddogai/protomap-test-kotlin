package com.acme.transport.safety.scorecard.window

/** A rule a primary scorecard segment must pass. Check returns its problems, or none. */
interface PrimaryScorecardSegmentRule {
    fun check(item: PrimaryScorecardSegment): MutableList<String>
}

data class PrimaryScorecardSegmentRequiredTitleRule(var message: String) : PrimaryScorecardSegmentRule {
    override fun check(item: PrimaryScorecardSegment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryScorecardSegmentNameLengthRule(var maxLength: Int) : PrimaryScorecardSegmentRule {
    override fun check(item: PrimaryScorecardSegment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryScorecardSegmentReviewerLengthRule(var maxLength: Int) : PrimaryScorecardSegmentRule {
    override fun check(item: PrimaryScorecardSegment): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary scorecard segment. */
data class PrimaryScorecardSegmentValidator(var rules: MutableList<PrimaryScorecardSegmentRule>) {
    fun validate(item: PrimaryScorecardSegment): MutableList<String> {
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

    fun requireValid(item: PrimaryScorecardSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryScorecardSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryScorecardSegmentValidator(): PrimaryScorecardSegmentValidator {
    return PrimaryScorecardSegmentValidator(
        mutableListOf<PrimaryScorecardSegmentRule>(
            PrimaryScorecardSegmentRequiredTitleRule("primary scorecard segment needs a value"),
            PrimaryScorecardSegmentNameLengthRule(1000),
            PrimaryScorecardSegmentReviewerLengthRule(86400),
        ),
    )
}
