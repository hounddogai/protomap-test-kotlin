package com.acme.transport.customs.clearance.policy

/** A rule a clearance snapshot must pass. Check returns its problems, or none. */
interface ClearanceSnapshotRule {
    fun check(item: ClearanceSnapshot): MutableList<String>
}

data class ClearanceSnapshotRetriesRangeRule(var min: Int, var max: Int) : ClearanceSnapshotRule {
    override fun check(item: ClearanceSnapshot): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class ClearanceSnapshotCodeLengthRule(var maxLength: Int) : ClearanceSnapshotRule {
    override fun check(item: ClearanceSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceSnapshotArchivedGuardRule(var limit: Int) : ClearanceSnapshotRule {
    override fun check(item: ClearanceSnapshot): MutableList<String> {
        if (item.archived && item.retries > limit) {
            return mutableListOf<String>("retries ${item.retries} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance snapshot. */
data class ClearanceSnapshotValidator(var rules: MutableList<ClearanceSnapshotRule>) {
    fun validate(item: ClearanceSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ClearanceSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceSnapshotValidator(): ClearanceSnapshotValidator {
    return ClearanceSnapshotValidator(
        mutableListOf<ClearanceSnapshotRule>(
            ClearanceSnapshotRetriesRangeRule(16, 80),
            ClearanceSnapshotCodeLengthRule(255),
            ClearanceSnapshotArchivedGuardRule(1000),
        ),
    )
}
