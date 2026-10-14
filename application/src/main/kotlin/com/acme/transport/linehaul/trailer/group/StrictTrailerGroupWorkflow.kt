package com.acme.transport.linehaul.trailer.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves strict trailer groups through their life cycle and keeps a journal of what it did. */
data class StrictTrailerGroupDesk(
    var store: StrictTrailerGroupStore,
    var validator: StrictTrailerGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictTrailerGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("strict trailer group $code not found")
        validator.requireValid(item)
        item.moveTo(StrictTrailerGroupStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("strict trailer group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictTrailerGroupStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("strict trailer group $code not found")
        val result = engine.compute(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newStrictTrailerGroupDesk(store: StrictTrailerGroupStore, clock: Clock, flags: Registry): StrictTrailerGroupDesk {
    return StrictTrailerGroupDesk(
        store = store,
        validator = defaultStrictTrailerGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictTrailerGroupEngine(flags),
    )
}
