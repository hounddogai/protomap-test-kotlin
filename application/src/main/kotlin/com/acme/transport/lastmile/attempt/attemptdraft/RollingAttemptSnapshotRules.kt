package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.kit.text.normalize

/** A rule a rolling attempt snapshot must pass. Check returns its problems, or none. */
interface RollingAttemptSnapshotRule {
    fun check(item: RollingAttemptSnapshot): MutableList<String>
}

data class RollingAttemptSnapshotAllowedReviewerRule(
    var allowed: MutableList<String>,
) : RollingAttemptSnapshotRule {
    override fun check(item: RollingAttemptSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class RollingAttemptSnapshotRequiredSourceRule(var message: String) : RollingAttemptSnapshotRule {
    override fun check(item: RollingAttemptSnapshot): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingAttemptSnapshotRequiredCodeRule(var message: String) : RollingAttemptSnapshotRule {
    override fun check(item: RollingAttemptSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling attempt snapshot. */
data class RollingAttemptSnapshotValidator(var rules: MutableList<RollingAttemptSnapshotRule>) {
    fun validate(item: RollingAttemptSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingAttemptSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingAttemptSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingAttemptSnapshotValidator(): RollingAttemptSnapshotValidator {
    return RollingAttemptSnapshotValidator(
        mutableListOf<RollingAttemptSnapshotRule>(
            RollingAttemptSnapshotAllowedReviewerRule(mutableListOf<String>("manual")),
            RollingAttemptSnapshotRequiredSourceRule("rolling attempt snapshot needs an entry"),
            RollingAttemptSnapshotRequiredCodeRule("rolling attempt snapshot needs a value"),
        ),
    )
}
