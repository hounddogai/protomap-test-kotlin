package com.acme.transport.network.zone.pendingzonemapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves pending zone mappings through their life cycle and keeps a journal of what it did. */
data class PendingZoneMappingService(
    var store: PendingZoneMappingStore,
    var validator: PendingZoneMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingZoneMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("pending zone mapping $code not found")
        validator.requireValid(item)
        item.moveTo(PendingZoneMappingStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("pending zone mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingZoneMappingStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("pending zone mapping $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newPendingZoneMappingService(store: PendingZoneMappingStore, clock: Clock, flags: Registry): PendingZoneMappingService {
    return PendingZoneMappingService(
        store = store,
        validator = defaultPendingZoneMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingZoneMappingEngine(flags),
    )
}
