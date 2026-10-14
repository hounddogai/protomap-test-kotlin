package com.acme.transport.linehaul.trailer.rollup

import com.acme.transport.linehaul.clock.Clock

/** Moves capped trailer rollups through their life cycle and keeps a journal of what it did. */
data class CappedTrailerRollupDesk(
    var store: CappedTrailerRollupStore,
    var validator: CappedTrailerRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped trailer rollup $code not found")
        validator.requireValid(item)
        item.moveTo(CappedTrailerRollupStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("capped trailer rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedTrailerRollupStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newCappedTrailerRollupDesk(store: CappedTrailerRollupStore, clock: Clock): CappedTrailerRollupDesk {
    return CappedTrailerRollupDesk(
        store = store,
        validator = defaultCappedTrailerRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
