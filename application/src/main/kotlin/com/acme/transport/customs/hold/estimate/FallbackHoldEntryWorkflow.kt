package com.acme.transport.customs.hold.estimate

import com.acme.transport.customs.clock.Clock

/** Moves fallback hold entries through their life cycle and keeps a journal of what it did. */
data class FallbackHoldEntryService(
    var store: FallbackHoldEntryStore,
    var validator: FallbackHoldEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("fallback hold entry $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackHoldEntryStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback hold entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackHoldEntryStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newFallbackHoldEntryService(store: FallbackHoldEntryStore, clock: Clock): FallbackHoldEntryService {
    return FallbackHoldEntryService(
        store = store,
        validator = defaultFallbackHoldEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
