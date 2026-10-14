package com.acme.transport.terminal.sortation.score

import com.acme.transport.terminal.clock.Clock

/** Moves fallback sortation records through their life cycle and keeps a journal of what it did. */
data class FallbackSortationRecordDesk(
    var store: FallbackSortationRecordStore,
    var validator: FallbackSortationRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackSortationRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback sortation record $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackSortationRecordStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback sortation record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackSortationRecordStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("fallback sortation record $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newFallbackSortationRecordDesk(store: FallbackSortationRecordStore, clock: Clock): FallbackSortationRecordDesk {
    return FallbackSortationRecordDesk(
        store = store,
        validator = defaultFallbackSortationRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackSortationRecordEngine(),
    )
}
