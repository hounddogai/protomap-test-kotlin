package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.customs.clock.Clock

/** Moves clearance templates through their life cycle and keeps a journal of what it did. */
data class ClearanceTemplateManager(
    var store: ClearanceTemplateStore,
    var validator: ClearanceTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("clearance template $code not found")
        validator.requireValid(item)
        item.moveTo(ClearanceTemplateStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("clearance template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ClearanceTemplateStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newClearanceTemplateManager(store: ClearanceTemplateStore, clock: Clock): ClearanceTemplateManager {
    return ClearanceTemplateManager(
        store = store,
        validator = defaultClearanceTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
