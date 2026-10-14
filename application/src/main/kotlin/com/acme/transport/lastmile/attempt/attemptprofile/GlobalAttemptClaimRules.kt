package com.acme.transport.lastmile.attempt.attemptprofile

/** A rule a global attempt claim must pass. Check returns its problems, or none. */
interface GlobalAttemptClaimRule {
    fun check(item: GlobalAttemptClaim): MutableList<String>
}

data class GlobalAttemptClaimCodeLengthRule(var maxLength: Int) : GlobalAttemptClaimRule {
    override fun check(item: GlobalAttemptClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAttemptClaimRequiredCarrierRule(var message: String) : GlobalAttemptClaimRule {
    override fun check(item: GlobalAttemptClaim): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalAttemptClaimTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : GlobalAttemptClaimRule {
    override fun check(item: GlobalAttemptClaim): MutableList<String> {
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

/** Runs every rule of a global attempt claim. */
data class GlobalAttemptClaimValidator(var rules: MutableList<GlobalAttemptClaimRule>) {
    fun validate(item: GlobalAttemptClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalAttemptClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalAttemptClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalAttemptClaimValidator(): GlobalAttemptClaimValidator {
    return GlobalAttemptClaimValidator(
        mutableListOf<GlobalAttemptClaimRule>(
            GlobalAttemptClaimCodeLengthRule(1000),
            GlobalAttemptClaimRequiredCarrierRule("global attempt claim needs this field"),
            GlobalAttemptClaimTagsLimitRule(3600, mutableListOf<String>("strict", "primary")),
        ),
    )
}
