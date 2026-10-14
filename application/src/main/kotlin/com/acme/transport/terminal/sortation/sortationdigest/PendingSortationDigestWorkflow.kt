package com.acme.transport.terminal.sortation.sortationdigest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/** Moves pending sortation digests through their life cycle and keeps a journal of what it did. */
data class PendingSortationDigestWorkflow(
    var store: PendingSortationDigestStore,
    var validator: PendingSortationDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingSortationDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("pending sortation digest $code not found")
        validator.requireValid(item)
        item.moveTo(PendingSortationDigestStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("pending sortation digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingSortationDigestStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("pending sortation digest $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newPendingSortationDigestWorkflow(store: PendingSortationDigestStore, clock: Clock, flags: Registry): PendingSortationDigestWorkflow {
    return PendingSortationDigestWorkflow(
        store = store,
        validator = defaultPendingSortationDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingSortationDigestEngine(flags),
    )
}
