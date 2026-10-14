package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.linehaul.clock.Clock

/** Moves global trailer cycles through their life cycle and keeps a journal of what it did. */
data class GlobalTrailerCycleDesk(
    var store: GlobalTrailerCycleStore,
    var validator: GlobalTrailerCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global trailer cycle $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalTrailerCycleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("global trailer cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalTrailerCycleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newGlobalTrailerCycleDesk(store: GlobalTrailerCycleStore, clock: Clock): GlobalTrailerCycleDesk {
    return GlobalTrailerCycleDesk(
        store = store,
        validator = defaultGlobalTrailerCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
