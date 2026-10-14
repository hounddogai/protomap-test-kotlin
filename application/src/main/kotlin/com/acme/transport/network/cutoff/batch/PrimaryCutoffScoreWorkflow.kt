package com.acme.transport.network.cutoff.batch

import com.acme.transport.network.clock.Clock

/** Moves primary cutoff scores through their life cycle and keeps a journal of what it did. */
data class PrimaryCutoffScoreManager(
    var store: PrimaryCutoffScoreStore,
    var validator: PrimaryCutoffScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("primary cutoff score $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryCutoffScoreStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("primary cutoff score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryCutoffScoreStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newPrimaryCutoffScoreManager(store: PrimaryCutoffScoreStore, clock: Clock): PrimaryCutoffScoreManager {
    return PrimaryCutoffScoreManager(
        store = store,
        validator = defaultPrimaryCutoffScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
