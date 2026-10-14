package com.acme.transport.terminal.traileryard.claim

/** A rule a scheduled trailer yard claim must pass. Check returns its problems, or none. */
interface ScheduledTrailerYardClaimRule {
    fun check(item: ScheduledTrailerYardClaim): MutableList<String>
}

data class ScheduledTrailerYardClaimNameLengthRule(var maxLength: Int) : ScheduledTrailerYardClaimRule {
    override fun check(item: ScheduledTrailerYardClaim): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerYardClaimCodeLengthRule(var maxLength: Int) : ScheduledTrailerYardClaimRule {
    override fun check(item: ScheduledTrailerYardClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerYardClaimArchivedGuardRule(var limit: Int) : ScheduledTrailerYardClaimRule {
    override fun check(item: ScheduledTrailerYardClaim): MutableList<String> {
        if (item.archived && item.windowDays > limit) {
            return mutableListOf<String>("window days ${item.windowDays} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled trailer yard claim. */
data class ScheduledTrailerYardClaimValidator(var rules: MutableList<ScheduledTrailerYardClaimRule>) {
    fun validate(item: ScheduledTrailerYardClaim): MutableList<String> {
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

    fun requireValid(item: ScheduledTrailerYardClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledTrailerYardClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledTrailerYardClaimValidator(): ScheduledTrailerYardClaimValidator {
    return ScheduledTrailerYardClaimValidator(
        mutableListOf<ScheduledTrailerYardClaimRule>(
            ScheduledTrailerYardClaimNameLengthRule(80),
            ScheduledTrailerYardClaimCodeLengthRule(80),
            ScheduledTrailerYardClaimArchivedGuardRule(16),
        ),
    )
}
