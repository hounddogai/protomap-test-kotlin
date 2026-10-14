package com.acme.transport.lastmile.drop.droprequest

import com.acme.transport.lastmile.clock.Clock

/** Moves lenient drop requests through their life cycle and keeps a journal of what it did. */
data class LenientDropRequestDesk(
    var store: LenientDropRequestStore,
    var validator: LenientDropRequestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient drop request $code not found")
        validator.requireValid(item)
        item.moveTo(LenientDropRequestStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient drop request $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientDropRequestStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newLenientDropRequestDesk(store: LenientDropRequestStore, clock: Clock): LenientDropRequestDesk {
    return LenientDropRequestDesk(
        store = store,
        validator = defaultLenientDropRequestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
