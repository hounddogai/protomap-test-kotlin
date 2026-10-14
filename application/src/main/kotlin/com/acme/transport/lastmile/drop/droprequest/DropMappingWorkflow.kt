package com.acme.transport.lastmile.drop.droprequest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves drop mappings through their life cycle and keeps a journal of what it did. */
data class DropMappingCoordinator(
    var store: DropMappingStore,
    var validator: DropMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DropMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("drop mapping $code not found")
        validator.requireValid(item)
        item.moveTo(DropMappingStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("drop mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DropMappingStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("drop mapping $code not found")
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

fun newDropMappingCoordinator(store: DropMappingStore, clock: Clock, flags: Registry): DropMappingCoordinator {
    return DropMappingCoordinator(
        store = store,
        validator = defaultDropMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDropMappingEngine(flags),
    )
}
