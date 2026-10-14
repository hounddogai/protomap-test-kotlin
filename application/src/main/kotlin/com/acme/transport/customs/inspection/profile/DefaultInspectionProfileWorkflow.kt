package com.acme.transport.customs.inspection.profile

import com.acme.transport.customs.clock.Clock

/** Moves default inspection profiles through their life cycle and keeps a journal of what it did. */
data class DefaultInspectionProfileManager(
    var store: DefaultInspectionProfileStore,
    var validator: DefaultInspectionProfileValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("default inspection profile $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultInspectionProfileStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("default inspection profile $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultInspectionProfileStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            val problems = validator.validate(item)
            if (problems.size == 0) {
                continue
            }
            note("${item.code} needs attention: ${problems.joinToString(", ")}")
            changed += 1
        }
        return changed
    }
}

fun newDefaultInspectionProfileManager(store: DefaultInspectionProfileStore, clock: Clock): DefaultInspectionProfileManager {
    return DefaultInspectionProfileManager(
        store = store,
        validator = defaultDefaultInspectionProfileValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
