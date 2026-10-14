package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.linehaul.clock.Clock

/** Moves capacity manifests through their life cycle and keeps a journal of what it did. */
data class CapacityManifestDesk(
    var store: CapacityManifestStore,
    var validator: CapacityManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CapacityManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capacity manifest $code not found")
        validator.requireValid(item)
        item.moveTo(CapacityManifestStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("capacity manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CapacityManifestStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("capacity manifest $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newCapacityManifestDesk(store: CapacityManifestStore, clock: Clock): CapacityManifestDesk {
    return CapacityManifestDesk(
        store = store,
        validator = defaultCapacityManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCapacityManifestEngine(),
    )
}
