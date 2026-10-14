package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves lenient drop targets through their life cycle and keeps a journal of what it did. */
data class LenientDropTargetDesk(
    var store: LenientDropTargetStore,
    var validator: LenientDropTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientDropTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lenient drop target $code not found")
        validator.requireValid(item)
        item.moveTo(LenientDropTargetStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient drop target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientDropTargetStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lenient drop target $code not found")
        val result = engine.evaluate(item)
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

fun newLenientDropTargetDesk(store: LenientDropTargetStore, clock: Clock, flags: Registry): LenientDropTargetDesk {
    return LenientDropTargetDesk(
        store = store,
        validator = defaultLenientDropTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientDropTargetEngine(flags),
    )
}
