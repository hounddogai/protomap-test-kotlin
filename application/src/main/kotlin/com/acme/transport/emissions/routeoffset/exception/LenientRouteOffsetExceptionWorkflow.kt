package com.acme.transport.emissions.routeoffset.exception

import com.acme.transport.emissions.clock.Clock

/**
 * Moves lenient route offset exceptions through their life cycle and keeps a journal of what it did.
 */
data class LenientRouteOffsetExceptionDesk(
    var store: LenientRouteOffsetExceptionStore,
    var validator: LenientRouteOffsetExceptionValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient route offset exception $code not found")
        validator.requireValid(item)
        item.moveTo(LenientRouteOffsetExceptionStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient route offset exception $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientRouteOffsetExceptionStatus.RETIRED)
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

fun newLenientRouteOffsetExceptionDesk(store: LenientRouteOffsetExceptionStore, clock: Clock): LenientRouteOffsetExceptionDesk {
    return LenientRouteOffsetExceptionDesk(
        store = store,
        validator = defaultLenientRouteOffsetExceptionValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
