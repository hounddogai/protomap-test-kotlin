package com.acme.transport.terminal.sortation.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves sortation exceptions through their life cycle and keeps a journal of what it did. */
data class SortationExceptionManager(
    var store: SortationExceptionStore,
    var validator: SortationExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: SortationExceptionEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("sortation exception $code not found")
        validator.requireValid(item)
        item.moveTo(SortationExceptionStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("sortation exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SortationExceptionStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("sortation exception $code not found")
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

fun newSortationExceptionManager(store: SortationExceptionStore, clock: Clock, flags: Registry): SortationExceptionManager {
    return SortationExceptionManager(
        store = store,
        validator = defaultSortationExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultSortationExceptionEngine(flags),
    )
}
