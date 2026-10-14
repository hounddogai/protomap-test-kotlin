package com.acme.transport.network.zone.draft

import com.acme.transport.network.clock.Clock

/** Moves rolling zone checkpoints through their life cycle and keeps a journal of what it did. */
data class RollingZoneCheckpointService(
    var store: RollingZoneCheckpointStore,
    var validator: RollingZoneCheckpointValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("rolling zone checkpoint $code not found")
        validator.requireValid(item)
        item.moveTo(RollingZoneCheckpointStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling zone checkpoint $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingZoneCheckpointStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newRollingZoneCheckpointService(store: RollingZoneCheckpointStore, clock: Clock): RollingZoneCheckpointService {
    return RollingZoneCheckpointService(
        store = store,
        validator = defaultRollingZoneCheckpointValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
