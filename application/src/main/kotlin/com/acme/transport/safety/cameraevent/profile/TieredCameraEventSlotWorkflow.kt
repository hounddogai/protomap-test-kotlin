package com.acme.transport.safety.cameraevent.profile

import com.acme.transport.safety.clock.Clock

/** Moves tiered camera event slots through their life cycle and keeps a journal of what it did. */
data class TieredCameraEventSlotCoordinator(
    var store: TieredCameraEventSlotStore,
    var validator: TieredCameraEventSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tiered camera event slot $code not found")
        validator.requireValid(item)
        item.moveTo(TieredCameraEventSlotStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered camera event slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredCameraEventSlotStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newTieredCameraEventSlotCoordinator(store: TieredCameraEventSlotStore, clock: Clock): TieredCameraEventSlotCoordinator {
    return TieredCameraEventSlotCoordinator(
        store = store,
        validator = defaultTieredCameraEventSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
