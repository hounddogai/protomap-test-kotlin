package com.acme.transport.network.hub.allocation

import com.acme.transport.network.clock.Clock

/** Moves capped hub mappings through their life cycle and keeps a journal of what it did. */
data class CappedHubMappingManager(
    var store: CappedHubMappingStore,
    var validator: CappedHubMappingValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedHubMappingEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capped hub mapping $code not found")
        validator.requireValid(item)
        item.moveTo(CappedHubMappingStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped hub mapping $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedHubMappingStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("capped hub mapping $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newCappedHubMappingManager(store: CappedHubMappingStore, clock: Clock): CappedHubMappingManager {
    return CappedHubMappingManager(
        store = store,
        validator = defaultCappedHubMappingValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedHubMappingEngine(),
    )
}
