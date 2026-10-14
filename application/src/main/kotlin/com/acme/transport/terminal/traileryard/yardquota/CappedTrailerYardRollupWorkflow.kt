package com.acme.transport.terminal.traileryard.yardquota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves capped trailer yard rollups through their life cycle and keeps a journal of what it did. */
data class CappedTrailerYardRollupService(
    var store: CappedTrailerYardRollupStore,
    var validator: CappedTrailerYardRollupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedTrailerYardRollupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("capped trailer yard rollup $code not found")
        validator.requireValid(item)
        item.moveTo(CappedTrailerYardRollupStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("capped trailer yard rollup $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedTrailerYardRollupStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("capped trailer yard rollup $code not found")
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

fun newCappedTrailerYardRollupService(store: CappedTrailerYardRollupStore, clock: Clock, flags: Registry): CappedTrailerYardRollupService {
    return CappedTrailerYardRollupService(
        store = store,
        validator = defaultCappedTrailerYardRollupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedTrailerYardRollupEngine(flags),
    )
}
