package com.acme.transport.lastmile.redelivery.forecast

import com.acme.transport.lastmile.clock.Clock

/** Moves lenient redelivery snapshots through their life cycle and keeps a journal of what it did. */
data class LenientRedeliverySnapshotService(
    var store: LenientRedeliverySnapshotStore,
    var validator: LenientRedeliverySnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientRedeliverySnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("lenient redelivery snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(LenientRedeliverySnapshotStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient redelivery snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientRedeliverySnapshotStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("lenient redelivery snapshot $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newLenientRedeliverySnapshotService(store: LenientRedeliverySnapshotStore, clock: Clock): LenientRedeliverySnapshotService {
    return LenientRedeliverySnapshotService(
        store = store,
        validator = defaultLenientRedeliverySnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientRedeliverySnapshotEngine(),
    )
}
