package com.acme.transport.terminal.crossdock.proposal

import com.acme.transport.kit.text.normalize

/** A rule a cross dock snapshot must pass. Check returns its problems, or none. */
interface CrossDockSnapshotRule {
    fun check(item: CrossDockSnapshot): MutableList<String>
}

data class CrossDockSnapshotAllowedCategoryRule(var allowed: MutableList<String>) : CrossDockSnapshotRule {
    override fun check(item: CrossDockSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockSnapshotRequiredNotesRule(var message: String) : CrossDockSnapshotRule {
    override fun check(item: CrossDockSnapshot): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockSnapshotRequiredCodeRule(var message: String) : CrossDockSnapshotRule {
    override fun check(item: CrossDockSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock snapshot. */
data class CrossDockSnapshotValidator(var rules: MutableList<CrossDockSnapshotRule>) {
    fun validate(item: CrossDockSnapshot): MutableList<String> {
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

    fun requireValid(item: CrossDockSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockSnapshotValidator(): CrossDockSnapshotValidator {
    return CrossDockSnapshotValidator(
        mutableListOf<CrossDockSnapshotRule>(
            CrossDockSnapshotAllowedCategoryRule(mutableListOf<String>()),
            CrossDockSnapshotRequiredNotesRule("cross dock snapshot needs an entry"),
            CrossDockSnapshotRequiredCodeRule("cross dock snapshot needs this field"),
        ),
    )
}
