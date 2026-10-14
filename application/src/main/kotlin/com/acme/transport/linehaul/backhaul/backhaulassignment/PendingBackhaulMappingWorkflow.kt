package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.linehaul.clock.Clock

/** Moves pending backhaul mappings through their life cycle and keeps a journal of what it did. */
data class PendingBackhaulMappingCoordinator(
    var store: PendingBackhaulMappingStore,
    var validator: PendingBackhaulMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingBackhaulMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("pending backhaul mapping $code not found")
        validator.requireValid(item)
        item.moveTo(PendingBackhaulMappingStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("pending backhaul mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingBackhaulMappingStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("pending backhaul mapping $code not found")
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

fun newPendingBackhaulMappingCoordinator(store: PendingBackhaulMappingStore, clock: Clock): PendingBackhaulMappingCoordinator {
    return PendingBackhaulMappingCoordinator(
        store = store,
        validator = defaultPendingBackhaulMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingBackhaulMappingEngine(),
    )
}
