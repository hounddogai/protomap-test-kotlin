package com.acme.transport.network.hub.exception

import com.acme.transport.network.clock.Clock

/** Moves tiered hub exceptions through their life cycle and keeps a journal of what it did. */
data class TieredHubExceptionWorkflow(
    var store: TieredHubExceptionStore,
    var validator: TieredHubExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tiered hub exception $code not found")
        validator.requireValid(item)
        item.moveTo(TieredHubExceptionStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered hub exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredHubExceptionStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newTieredHubExceptionWorkflow(store: TieredHubExceptionStore, clock: Clock): TieredHubExceptionWorkflow {
    return TieredHubExceptionWorkflow(
        store = store,
        validator = defaultTieredHubExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
