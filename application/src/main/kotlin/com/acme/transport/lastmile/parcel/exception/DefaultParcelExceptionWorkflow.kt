package com.acme.transport.lastmile.parcel.exception

import com.acme.transport.lastmile.clock.Clock

/** Moves default parcel exceptions through their life cycle and keeps a journal of what it did. */
data class DefaultParcelExceptionDesk(
    var store: DefaultParcelExceptionStore,
    var validator: DefaultParcelExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default parcel exception $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultParcelExceptionStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("default parcel exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultParcelExceptionStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newDefaultParcelExceptionDesk(store: DefaultParcelExceptionStore, clock: Clock): DefaultParcelExceptionDesk {
    return DefaultParcelExceptionDesk(
        store = store,
        validator = defaultDefaultParcelExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
