package com.acme.transport.lastmile.attempt.tier

import com.acme.transport.kit.text.normalize

/** A rule a global attempt record must pass. Check returns its problems, or none. */
interface GlobalAttemptRecordRule {
    fun check(item: GlobalAttemptRecord): MutableList<String>
}

data class GlobalAttemptRecordRequiredCodeRule(var message: String) : GlobalAttemptRecordRule {
    override fun check(item: GlobalAttemptRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalAttemptRecordRequiredReviewerRule(var message: String) : GlobalAttemptRecordRule {
    override fun check(item: GlobalAttemptRecord): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalAttemptRecordArchivedGuardRule(var limit: Int) : GlobalAttemptRecordRule {
    override fun check(item: GlobalAttemptRecord): MutableList<String> {
        if (item.archived && item.revision > limit) {
            return mutableListOf<String>("revision ${item.revision} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAttemptRecordAllowedLabelRule(var allowed: MutableList<String>) : GlobalAttemptRecordRule {
    override fun check(item: GlobalAttemptRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global attempt record. */
data class GlobalAttemptRecordValidator(var rules: MutableList<GlobalAttemptRecordRule>) {
    fun validate(item: GlobalAttemptRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalAttemptRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalAttemptRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalAttemptRecordValidator(): GlobalAttemptRecordValidator {
    return GlobalAttemptRecordValidator(
        mutableListOf<GlobalAttemptRecordRule>(
            GlobalAttemptRecordRequiredCodeRule("global attempt record needs this field"),
            GlobalAttemptRecordRequiredReviewerRule("global attempt record needs an entry"),
            GlobalAttemptRecordArchivedGuardRule(1000),
            GlobalAttemptRecordAllowedLabelRule(mutableListOf<String>("manual", "tiered", "rolling")),
        ),
    )
}
