package com.acme.transport.safety.cameraevent.eventslot

import com.acme.transport.safety.clock.Clock

/**
 * Moves fallback camera event snapshots through their life cycle and keeps a journal of what it did.
 */
data class FallbackCameraEventSnapshotService(
    var store: FallbackCameraEventSnapshotStore,
    var validator: FallbackCameraEventSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackCameraEventSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback camera event snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackCameraEventSnapshotStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback camera event snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackCameraEventSnapshotStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fallback camera event snapshot $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
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

fun newFallbackCameraEventSnapshotService(store: FallbackCameraEventSnapshotStore, clock: Clock): FallbackCameraEventSnapshotService {
    return FallbackCameraEventSnapshotService(
        store = store,
        validator = defaultFallbackCameraEventSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackCameraEventSnapshotEngine(),
    )
}
