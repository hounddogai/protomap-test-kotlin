package com.acme.transport.terminal.dockdoor.schedule

import com.acme.transport.kit.text.normalize

/** A rule a pending dock door draft must pass. Check returns its problems, or none. */
interface PendingDockDoorDraftRule {
    fun check(item: PendingDockDoorDraft): MutableList<String>
}

data class PendingDockDoorDraftAllowedTitleRule(var allowed: MutableList<String>) : PendingDockDoorDraftRule {
    override fun check(item: PendingDockDoorDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class PendingDockDoorDraftArchivedGuardRule(var limit: Double) : PendingDockDoorDraftRule {
    override fun check(item: PendingDockDoorDraft): MutableList<String> {
        if (item.archived && item.loadKg > limit) {
            return mutableListOf<String>("load kg ${item.loadKg} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class PendingDockDoorDraftRequiredLaneRule(var message: String) : PendingDockDoorDraftRule {
    override fun check(item: PendingDockDoorDraft): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDockDoorDraftRequiredTitleRule(var message: String) : PendingDockDoorDraftRule {
    override fun check(item: PendingDockDoorDraft): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending dock door draft. */
data class PendingDockDoorDraftValidator(var rules: MutableList<PendingDockDoorDraftRule>) {
    fun validate(item: PendingDockDoorDraft): MutableList<String> {
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

    fun requireValid(item: PendingDockDoorDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDockDoorDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDockDoorDraftValidator(): PendingDockDoorDraftValidator {
    return PendingDockDoorDraftValidator(
        mutableListOf<PendingDockDoorDraftRule>(
            PendingDockDoorDraftAllowedTitleRule(mutableListOf<String>()),
            PendingDockDoorDraftArchivedGuardRule(5.7),
            PendingDockDoorDraftRequiredLaneRule("pending dock door draft needs an entry"),
            PendingDockDoorDraftRequiredTitleRule("pending dock door draft needs a value"),
        ),
    )
}
