package com.acme.transport.safety.cameraevent.evententry

import com.acme.transport.safety.clock.Clock

/**
 * Moves scheduled camera event entries through their life cycle and keeps a journal of what it did.
 */
data class ScheduledCameraEventEntryWorkflow(
    var store: ScheduledCameraEventEntryStore,
    var validator: ScheduledCameraEventEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled camera event entry $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledCameraEventEntryStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled camera event entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledCameraEventEntryStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newScheduledCameraEventEntryWorkflow(store: ScheduledCameraEventEntryStore, clock: Clock): ScheduledCameraEventEntryWorkflow {
    return ScheduledCameraEventEntryWorkflow(
        store = store,
        validator = defaultScheduledCameraEventEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
