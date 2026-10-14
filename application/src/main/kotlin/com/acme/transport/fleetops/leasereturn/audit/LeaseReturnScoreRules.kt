package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.kit.text.normalize

/** A rule a lease return score must pass. Check returns its problems, or none. */
interface LeaseReturnScoreRule {
    fun check(item: LeaseReturnScore): MutableList<String>
}

data class LeaseReturnScoreNameLengthRule(var maxLength: Int) : LeaseReturnScoreRule {
    override fun check(item: LeaseReturnScore): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnScoreUnitLengthRule(var maxLength: Int) : LeaseReturnScoreRule {
    override fun check(item: LeaseReturnScore): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnScoreCodePrefixRule(var prefix: String) : LeaseReturnScoreRule {
    override fun check(item: LeaseReturnScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnScoreAllowedLabelRule(var allowed: MutableList<String>) : LeaseReturnScoreRule {
    override fun check(item: LeaseReturnScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is stale")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnScoreRequiredCodeRule(var message: String) : LeaseReturnScoreRule {
    override fun check(item: LeaseReturnScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnScoreRequiredUnitRule(var message: String) : LeaseReturnScoreRule {
    override fun check(item: LeaseReturnScore): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return score. */
data class LeaseReturnScoreValidator(var rules: MutableList<LeaseReturnScoreRule>) {
    fun validate(item: LeaseReturnScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LeaseReturnScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnScore>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnScoreValidator(): LeaseReturnScoreValidator {
    return LeaseReturnScoreValidator(
        mutableListOf<LeaseReturnScoreRule>(
            LeaseReturnScoreNameLengthRule(3600),
            LeaseReturnScoreUnitLengthRule(16),
            LeaseReturnScoreCodePrefixRule("Q"),
            LeaseReturnScoreAllowedLabelRule(mutableListOf<String>("seasonal", "fallback")),
            LeaseReturnScoreRequiredCodeRule("lease return score needs this field"),
            LeaseReturnScoreRequiredUnitRule("lease return score needs a value"),
        ),
    )
}
