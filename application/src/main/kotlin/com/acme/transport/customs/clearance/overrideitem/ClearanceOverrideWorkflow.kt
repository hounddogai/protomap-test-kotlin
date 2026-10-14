package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.customs.clock.Clock

/** Moves clearance overrides through their life cycle and keeps a journal of what it did. */
data class ClearanceOverrideManager(
    var store: ClearanceOverrideStore,
    var validator: ClearanceOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ClearanceOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("clearance override $code not found")
        validator.requireValid(item)
        item.moveTo(ClearanceOverrideStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("clearance override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ClearanceOverrideStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("clearance override $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newClearanceOverrideManager(store: ClearanceOverrideStore, clock: Clock): ClearanceOverrideManager {
    return ClearanceOverrideManager(
        store = store,
        validator = defaultClearanceOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultClearanceOverrideEngine(),
    )
}
