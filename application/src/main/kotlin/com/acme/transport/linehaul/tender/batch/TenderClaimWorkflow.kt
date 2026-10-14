package com.acme.transport.linehaul.tender.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves tender claims through their life cycle and keeps a journal of what it did. */
data class TenderClaimWorkflow(
    var store: TenderClaimStore,
    var validator: TenderClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TenderClaimEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("tender claim $code not found")
        validator.requireValid(item)
        item.moveTo(TenderClaimStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("tender claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TenderClaimStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("tender claim $code not found")
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

fun newTenderClaimWorkflow(store: TenderClaimStore, clock: Clock, flags: Registry): TenderClaimWorkflow {
    return TenderClaimWorkflow(
        store = store,
        validator = defaultTenderClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTenderClaimEngine(flags),
    )
}
