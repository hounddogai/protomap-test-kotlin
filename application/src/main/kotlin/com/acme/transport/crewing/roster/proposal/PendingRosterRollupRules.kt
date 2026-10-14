package com.acme.transport.crewing.roster.proposal

import com.acme.transport.kit.text.normalize

/** A rule a pending roster rollup must pass. Check returns its problems, or none. */
interface PendingRosterRollupRule {
    fun check(item: PendingRosterRollup): MutableList<String>
}

data class PendingRosterRollupRequiredCodeRule(var message: String) : PendingRosterRollupRule {
    override fun check(item: PendingRosterRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRosterRollupAllowedOwnerIdRule(var allowed: MutableList<String>) : PendingRosterRollupRule {
    override fun check(item: PendingRosterRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class PendingRosterRollupDepotLengthRule(var maxLength: Int) : PendingRosterRollupRule {
    override fun check(item: PendingRosterRollup): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRosterRollupTitleLengthRule(var maxLength: Int) : PendingRosterRollupRule {
    override fun check(item: PendingRosterRollup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRosterRollupRequiredDepotRule(var message: String) : PendingRosterRollupRule {
    override fun check(item: PendingRosterRollup): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRosterRollupCodeLengthRule(var maxLength: Int) : PendingRosterRollupRule {
    override fun check(item: PendingRosterRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending roster rollup. */
data class PendingRosterRollupValidator(var rules: MutableList<PendingRosterRollupRule>) {
    fun validate(item: PendingRosterRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingRosterRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingRosterRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingRosterRollupValidator(): PendingRosterRollupValidator {
    return PendingRosterRollupValidator(
        mutableListOf<PendingRosterRollupRule>(
            PendingRosterRollupRequiredCodeRule("pending roster rollup needs an entry"),
            PendingRosterRollupAllowedOwnerIdRule(mutableListOf<String>("strict", "tiered", "legacy")),
            PendingRosterRollupDepotLengthRule(255),
            PendingRosterRollupTitleLengthRule(120),
            PendingRosterRollupRequiredDepotRule("pending roster rollup needs this field"),
            PendingRosterRollupCodeLengthRule(3600),
        ),
    )
}
