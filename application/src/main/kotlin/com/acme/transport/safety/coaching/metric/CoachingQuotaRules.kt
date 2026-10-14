package com.acme.transport.safety.coaching.metric

import com.acme.transport.kit.text.normalize

/** A rule a coaching quota must pass. Check returns its problems, or none. */
interface CoachingQuotaRule {
    fun check(item: CoachingQuota): MutableList<String>
}

data class CoachingQuotaAllowedCarrierRule(var allowed: MutableList<String>) : CoachingQuotaRule {
    override fun check(item: CoachingQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class CoachingQuotaRequiredTitleRule(var message: String) : CoachingQuotaRule {
    override fun check(item: CoachingQuota): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingQuotaPinnedGuardRule(var limit: Int) : CoachingQuotaRule {
    override fun check(item: CoachingQuota): MutableList<String> {
        if (item.pinned && item.createdAt > limit) {
            return mutableListOf<String>("created at ${item.createdAt} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching quota. */
data class CoachingQuotaValidator(var rules: MutableList<CoachingQuotaRule>) {
    fun validate(item: CoachingQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CoachingQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingQuotaValidator(): CoachingQuotaValidator {
    return CoachingQuotaValidator(
        mutableListOf<CoachingQuotaRule>(
            CoachingQuotaAllowedCarrierRule(mutableListOf<String>("scheduled", "pending", "primary")),
            CoachingQuotaRequiredTitleRule("coaching quota needs a value"),
            CoachingQuotaPinnedGuardRule(64),
        ),
    )
}
