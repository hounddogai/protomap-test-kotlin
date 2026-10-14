package com.acme.transport.linehaul.trailer.request

/** A rule a global trailer claim must pass. Check returns its problems, or none. */
interface GlobalTrailerClaimRule {
    fun check(item: GlobalTrailerClaim): MutableList<String>
}

data class GlobalTrailerClaimTitleLengthRule(var maxLength: Int) : GlobalTrailerClaimRule {
    override fun check(item: GlobalTrailerClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerClaimArchivedGuardRule(var limit: Int) : GlobalTrailerClaimRule {
    override fun check(item: GlobalTrailerClaim): MutableList<String> {
        if (item.archived && item.limit > limit) {
            return mutableListOf<String>("limit ${item.limit} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerClaimCodeLengthRule(var maxLength: Int) : GlobalTrailerClaimRule {
    override fun check(item: GlobalTrailerClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global trailer claim. */
data class GlobalTrailerClaimValidator(var rules: MutableList<GlobalTrailerClaimRule>) {
    fun validate(item: GlobalTrailerClaim): MutableList<String> {
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

    fun requireValid(item: GlobalTrailerClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTrailerClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTrailerClaimValidator(): GlobalTrailerClaimValidator {
    return GlobalTrailerClaimValidator(
        mutableListOf<GlobalTrailerClaimRule>(
            GlobalTrailerClaimTitleLengthRule(64),
            GlobalTrailerClaimArchivedGuardRule(16),
            GlobalTrailerClaimCodeLengthRule(64),
        ),
    )
}
