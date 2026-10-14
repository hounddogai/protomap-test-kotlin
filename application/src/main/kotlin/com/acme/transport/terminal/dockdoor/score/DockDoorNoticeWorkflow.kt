package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.terminal.clock.Clock

/** Moves dock door notices through their life cycle and keeps a journal of what it did. */
data class DockDoorNoticeWorkflow(
    var store: DockDoorNoticeStore,
    var validator: DockDoorNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("dock door notice $code not found")
        validator.requireValid(item)
        item.moveTo(DockDoorNoticeStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("dock door notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DockDoorNoticeStatus.WITHDRAWN)
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

fun newDockDoorNoticeWorkflow(store: DockDoorNoticeStore, clock: Clock): DockDoorNoticeWorkflow {
    return DockDoorNoticeWorkflow(
        store = store,
        validator = defaultDockDoorNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
