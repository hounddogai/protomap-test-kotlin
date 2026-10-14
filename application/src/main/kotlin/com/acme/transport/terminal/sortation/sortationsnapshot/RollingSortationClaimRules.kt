package com.acme.transport.terminal.sortation.sortationsnapshot

/** A rule a rolling sortation claim must pass. Check returns its problems, or none. */
interface RollingSortationClaimRule {
    fun check(item: RollingSortationClaim): MutableList<String>
}

data class RollingSortationClaimArchivedGuardRule(var limit: Int) : RollingSortationClaimRule {
    override fun check(item: RollingSortationClaim): MutableList<String> {
        if (item.archived && item.windowDays > limit) {
            return mutableListOf<String>("window days ${item.windowDays} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class RollingSortationClaimRequiredLabelRule(var message: String) : RollingSortationClaimRule {
    override fun check(item: RollingSortationClaim): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingSortationClaimRequiredTrailerIdRule(var message: String) : RollingSortationClaimRule {
    override fun check(item: RollingSortationClaim): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingSortationClaimTrailerIdLengthRule(var maxLength: Int) : RollingSortationClaimRule {
    override fun check(item: RollingSortationClaim): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling sortation claim. */
data class RollingSortationClaimValidator(var rules: MutableList<RollingSortationClaimRule>) {
    fun validate(item: RollingSortationClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingSortationClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingSortationClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingSortationClaimValidator(): RollingSortationClaimValidator {
    return RollingSortationClaimValidator(
        mutableListOf<RollingSortationClaimRule>(
            RollingSortationClaimArchivedGuardRule(255),
            RollingSortationClaimRequiredLabelRule("rolling sortation claim needs an entry"),
            RollingSortationClaimRequiredTrailerIdRule("rolling sortation claim needs a value"),
            RollingSortationClaimTrailerIdLengthRule(1000),
        ),
    )
}
