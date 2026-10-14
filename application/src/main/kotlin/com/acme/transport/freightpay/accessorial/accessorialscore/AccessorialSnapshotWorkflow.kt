package com.acme.transport.freightpay.accessorial.accessorialscore

import com.acme.transport.freightpay.clock.Clock

/** Moves accessorial snapshots through their life cycle and keeps a journal of what it did. */
data class AccessorialSnapshotManager(
    var store: AccessorialSnapshotStore,
    var validator: AccessorialSnapshotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: AccessorialSnapshotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("accessorial snapshot $code not found")
        validator.requireValid(item)
        item.moveTo(AccessorialSnapshotStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("accessorial snapshot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AccessorialSnapshotStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("accessorial snapshot $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.createdAt > cutoff) {
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

fun newAccessorialSnapshotManager(store: AccessorialSnapshotStore, clock: Clock): AccessorialSnapshotManager {
    return AccessorialSnapshotManager(
        store = store,
        validator = defaultAccessorialSnapshotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultAccessorialSnapshotEngine(),
    )
}
