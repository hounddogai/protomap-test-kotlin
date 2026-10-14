package com.acme.transport.safety.nearmiss.missrecord

import com.acme.transport.kit.text.normalize

/** A rule a pending near miss milestone must pass. Check returns its problems, or none. */
interface PendingNearMissMilestoneRule {
    fun check(item: PendingNearMissMilestone): MutableList<String>
}

data class PendingNearMissMilestoneAllowedNameRule(
    var allowed: MutableList<String>,
) : PendingNearMissMilestoneRule {
    override fun check(item: PendingNearMissMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is expired")
        }
        return mutableListOf<String>()
    }
}

data class PendingNearMissMilestoneArchivedGuardRule(var limit: Double) : PendingNearMissMilestoneRule {
    override fun check(item: PendingNearMissMilestone): MutableList<String> {
        if (item.archived && item.maximum > limit) {
            return mutableListOf<String>("maximum ${item.maximum} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class PendingNearMissMilestoneCodeLengthRule(var maxLength: Int) : PendingNearMissMilestoneRule {
    override fun check(item: PendingNearMissMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingNearMissMilestoneRequiredNameRule(var message: String) : PendingNearMissMilestoneRule {
    override fun check(item: PendingNearMissMilestone): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingNearMissMilestoneCodePrefixRule(var prefix: String) : PendingNearMissMilestoneRule {
    override fun check(item: PendingNearMissMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingNearMissMilestoneRequiredCountryRule(var message: String) : PendingNearMissMilestoneRule {
    override fun check(item: PendingNearMissMilestone): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending near miss milestone. */
data class PendingNearMissMilestoneValidator(var rules: MutableList<PendingNearMissMilestoneRule>) {
    fun validate(item: PendingNearMissMilestone): MutableList<String> {
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

    fun requireValid(item: PendingNearMissMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingNearMissMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingNearMissMilestoneValidator(): PendingNearMissMilestoneValidator {
    return PendingNearMissMilestoneValidator(
        mutableListOf<PendingNearMissMilestoneRule>(
            PendingNearMissMilestoneAllowedNameRule(mutableListOf<String>("primary", "adaptive")),
            PendingNearMissMilestoneArchivedGuardRule(2.1),
            PendingNearMissMilestoneCodeLengthRule(32),
            PendingNearMissMilestoneRequiredNameRule("pending near miss milestone needs an entry"),
            PendingNearMissMilestoneCodePrefixRule("Q"),
            PendingNearMissMilestoneRequiredCountryRule("pending near miss milestone needs an entry"),
        ),
    )
}
