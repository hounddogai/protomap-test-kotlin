package com.acme.transport.terminal.dockdoor.agreement

import com.acme.transport.kit.text.normalize

/** A rule a dock door snapshot must pass. Check returns its problems, or none. */
interface DockDoorSnapshotRule {
    fun check(item: DockDoorSnapshot): MutableList<String>
}

data class DockDoorSnapshotAllowedPhoneRule(var allowed: MutableList<String>) : DockDoorSnapshotRule {
    override fun check(item: DockDoorSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorSnapshotRequiredDockRule(var message: String) : DockDoorSnapshotRule {
    override fun check(item: DockDoorSnapshot): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorSnapshotRequiredTrailerIdRule(var message: String) : DockDoorSnapshotRule {
    override fun check(item: DockDoorSnapshot): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door snapshot. */
data class DockDoorSnapshotValidator(var rules: MutableList<DockDoorSnapshotRule>) {
    fun validate(item: DockDoorSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DockDoorSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorSnapshotValidator(): DockDoorSnapshotValidator {
    return DockDoorSnapshotValidator(
        mutableListOf<DockDoorSnapshotRule>(
            DockDoorSnapshotAllowedPhoneRule(mutableListOf<String>("default", "legacy", "scheduled")),
            DockDoorSnapshotRequiredDockRule("dock door snapshot needs an entry"),
            DockDoorSnapshotRequiredTrailerIdRule("dock door snapshot needs an entry"),
        ),
    )
}
