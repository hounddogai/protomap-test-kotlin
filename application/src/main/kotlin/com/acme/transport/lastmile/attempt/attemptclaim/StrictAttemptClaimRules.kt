package com.acme.transport.lastmile.attempt.attemptclaim

/** A rule a strict attempt claim must pass. Check returns its problems, or none. */
interface StrictAttemptClaimRule {
    fun check(item: StrictAttemptClaim): MutableList<String>
}

data class StrictAttemptClaimRequiredCodeRule(var message: String) : StrictAttemptClaimRule {
    override fun check(item: StrictAttemptClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictAttemptClaimStartsAtRangeRule(var min: Int, var max: Int) : StrictAttemptClaimRule {
    override fun check(item: StrictAttemptClaim): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class StrictAttemptClaimTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : StrictAttemptClaimRule {
    override fun check(item: StrictAttemptClaim): MutableList<String> {
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

/** Runs every rule of a strict attempt claim. */
data class StrictAttemptClaimValidator(var rules: MutableList<StrictAttemptClaimRule>) {
    fun validate(item: StrictAttemptClaim): MutableList<String> {
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

    fun requireValid(item: StrictAttemptClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictAttemptClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictAttemptClaimValidator(): StrictAttemptClaimValidator {
    return StrictAttemptClaimValidator(
        mutableListOf<StrictAttemptClaimRule>(
            StrictAttemptClaimRequiredCodeRule("strict attempt claim needs an entry"),
            StrictAttemptClaimStartsAtRangeRule(16, 64),
            StrictAttemptClaimTagsLimitRule(32, mutableListOf<String>("manual")),
        ),
    )
}
