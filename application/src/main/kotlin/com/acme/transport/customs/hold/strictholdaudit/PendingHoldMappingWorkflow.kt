package com.acme.transport.customs.hold.strictholdaudit

import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves pending hold mappings through their life cycle and keeps a journal of what it did. */
data class PendingHoldMappingManager(
    var store: PendingHoldMappingStore,
    var validator: PendingHoldMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingHoldMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("pending hold mapping $code not found")
        validator.requireValid(item)
        item.moveTo(PendingHoldMappingStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("pending hold mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingHoldMappingStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("pending hold mapping $code not found")
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

fun newPendingHoldMappingManager(store: PendingHoldMappingStore, clock: Clock, flags: Registry): PendingHoldMappingManager {
    return PendingHoldMappingManager(
        store = store,
        validator = defaultPendingHoldMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingHoldMappingEngine(flags),
    )
}
