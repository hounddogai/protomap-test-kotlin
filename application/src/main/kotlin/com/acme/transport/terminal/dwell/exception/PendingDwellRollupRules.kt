package com.acme.transport.terminal.dwell.exception

import com.acme.transport.kit.text.normalize

/** A rule a pending dwell rollup must pass. Check returns its problems, or none. */
interface PendingDwellRollupRule {
    fun check(item: PendingDwellRollup): MutableList<String>
}

data class PendingDwellRollupAllowedOwnerIdRule(var allowed: MutableList<String>) : PendingDwellRollupRule {
    override fun check(item: PendingDwellRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is missing")
        }
        return mutableListOf<String>()
    }
}

data class PendingDwellRollupNotesLengthRule(var maxLength: Int) : PendingDwellRollupRule {
    override fun check(item: PendingDwellRollup): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingDwellRollupRequiredNotesRule(var message: String) : PendingDwellRollupRule {
    override fun check(item: PendingDwellRollup): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending dwell rollup. */
data class PendingDwellRollupValidator(var rules: MutableList<PendingDwellRollupRule>) {
    fun validate(item: PendingDwellRollup): MutableList<String> {
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

    fun requireValid(item: PendingDwellRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDwellRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDwellRollupValidator(): PendingDwellRollupValidator {
    return PendingDwellRollupValidator(
        mutableListOf<PendingDwellRollupRule>(
            PendingDwellRollupAllowedOwnerIdRule(mutableListOf<String>("lenient", "capped", "regional")),
            PendingDwellRollupNotesLengthRule(16),
            PendingDwellRollupRequiredNotesRule("pending dwell rollup needs this field"),
        ),
    )
}
