package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import com.acme.transport.lastmile.clock.Clock

/** Moves scheduled parcel mappings through their life cycle and keeps a journal of what it did. */
data class ScheduledParcelMappingWorkflow(
    var store: ScheduledParcelMappingStore,
    var validator: ScheduledParcelMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("scheduled parcel mapping $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledParcelMappingStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled parcel mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledParcelMappingStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newScheduledParcelMappingWorkflow(store: ScheduledParcelMappingStore, clock: Clock): ScheduledParcelMappingWorkflow {
    return ScheduledParcelMappingWorkflow(
        store = store,
        validator = defaultScheduledParcelMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
