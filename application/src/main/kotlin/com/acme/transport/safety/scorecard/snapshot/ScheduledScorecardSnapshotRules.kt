package com.acme.transport.safety.scorecard.snapshot

/** A rule a scheduled scorecard snapshot must pass. Check returns its problems, or none. */
interface ScheduledScorecardSnapshotRule {
    fun check(item: ScheduledScorecardSnapshot): MutableList<String>
}

data class ScheduledScorecardSnapshotTrailerIdLengthRule(var maxLength: Int) : ScheduledScorecardSnapshotRule {
    override fun check(item: ScheduledScorecardSnapshot): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledScorecardSnapshotTitleLengthRule(var maxLength: Int) : ScheduledScorecardSnapshotRule {
    override fun check(item: ScheduledScorecardSnapshot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledScorecardSnapshotRequiredTitleRule(var message: String) : ScheduledScorecardSnapshotRule {
    override fun check(item: ScheduledScorecardSnapshot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled scorecard snapshot. */
data class ScheduledScorecardSnapshotValidator(var rules: MutableList<ScheduledScorecardSnapshotRule>) {
    fun validate(item: ScheduledScorecardSnapshot): MutableList<String> {
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

    fun requireValid(item: ScheduledScorecardSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledScorecardSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledScorecardSnapshotValidator(): ScheduledScorecardSnapshotValidator {
    return ScheduledScorecardSnapshotValidator(
        mutableListOf<ScheduledScorecardSnapshotRule>(
            ScheduledScorecardSnapshotTrailerIdLengthRule(86400),
            ScheduledScorecardSnapshotTitleLengthRule(3600),
            ScheduledScorecardSnapshotRequiredTitleRule("scheduled scorecard snapshot needs a value"),
        ),
    )
}
