package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves lenient accessorial proposals through their life cycle and keeps a journal of what it did. */
data class LenientAccessorialProposalDesk(
    var store: LenientAccessorialProposalStore,
    var validator: LenientAccessorialProposalValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientAccessorialProposalEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lenient accessorial proposal $code not found")
        validator.requireValid(item)
        item.moveTo(LenientAccessorialProposalStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient accessorial proposal $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientAccessorialProposalStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("lenient accessorial proposal $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newLenientAccessorialProposalDesk(store: LenientAccessorialProposalStore, clock: Clock, flags: Registry): LenientAccessorialProposalDesk {
    return LenientAccessorialProposalDesk(
        store = store,
        validator = defaultLenientAccessorialProposalValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientAccessorialProposalEngine(flags),
    )
}
