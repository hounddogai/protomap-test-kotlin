package com.acme.transport.terminal.dockdoor.exception

import com.acme.transport.terminal.clock.Clock

/** Moves scheduled dock door notices through their life cycle and keeps a journal of what it did. */
data class ScheduledDockDoorNoticeManager(
    var store: ScheduledDockDoorNoticeStore,
    var validator: ScheduledDockDoorNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled dock door notice $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledDockDoorNoticeStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled dock door notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledDockDoorNoticeStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newScheduledDockDoorNoticeManager(store: ScheduledDockDoorNoticeStore, clock: Clock): ScheduledDockDoorNoticeManager {
    return ScheduledDockDoorNoticeManager(
        store = store,
        validator = defaultScheduledDockDoorNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
