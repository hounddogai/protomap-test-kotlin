package com.acme.transport.fleetops.utilization.exception

/** A rule a pending utilization digest must pass. Check returns its problems, or none. */
interface PendingUtilizationDigestRule {
    fun check(item: PendingUtilizationDigest): MutableList<String>
}

data class PendingUtilizationDigestCodeLengthRule(var maxLength: Int) : PendingUtilizationDigestRule {
    override fun check(item: PendingUtilizationDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingUtilizationDigestArchivedGuardRule(var limit: Double) : PendingUtilizationDigestRule {
    override fun check(item: PendingUtilizationDigest): MutableList<String> {
        if (item.archived && item.loadKg > limit) {
            return mutableListOf<String>("load kg ${item.loadKg} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class PendingUtilizationDigestRequiredTitleRule(var message: String) : PendingUtilizationDigestRule {
    override fun check(item: PendingUtilizationDigest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending utilization digest. */
data class PendingUtilizationDigestValidator(var rules: MutableList<PendingUtilizationDigestRule>) {
    fun validate(item: PendingUtilizationDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingUtilizationDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingUtilizationDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingUtilizationDigestValidator(): PendingUtilizationDigestValidator {
    return PendingUtilizationDigestValidator(
        mutableListOf<PendingUtilizationDigestRule>(
            PendingUtilizationDigestCodeLengthRule(120),
            PendingUtilizationDigestArchivedGuardRule(8.7),
            PendingUtilizationDigestRequiredTitleRule("pending utilization digest needs this field"),
        ),
    )
}
