package com.acme.transport.fleetops.leasereturn.template

import com.acme.transport.kit.text.normalize

/** A rule a lease return checkpoint must pass. Check returns its problems, or none. */
interface LeaseReturnCheckpointRule {
    fun check(item: LeaseReturnCheckpoint): MutableList<String>
}

data class LeaseReturnCheckpointCodePrefixRule(var prefix: String) : LeaseReturnCheckpointRule {
    override fun check(item: LeaseReturnCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnCheckpointCodeLengthRule(var maxLength: Int) : LeaseReturnCheckpointRule {
    override fun check(item: LeaseReturnCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnCheckpointAllowedTitleRule(var allowed: MutableList<String>) : LeaseReturnCheckpointRule {
    override fun check(item: LeaseReturnCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return checkpoint. */
data class LeaseReturnCheckpointValidator(var rules: MutableList<LeaseReturnCheckpointRule>) {
    fun validate(item: LeaseReturnCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LeaseReturnCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnCheckpointValidator(): LeaseReturnCheckpointValidator {
    return LeaseReturnCheckpointValidator(
        mutableListOf<LeaseReturnCheckpointRule>(
            LeaseReturnCheckpointCodePrefixRule("REF"),
            LeaseReturnCheckpointCodeLengthRule(120),
            LeaseReturnCheckpointAllowedTitleRule(mutableListOf<String>()),
        ),
    )
}
