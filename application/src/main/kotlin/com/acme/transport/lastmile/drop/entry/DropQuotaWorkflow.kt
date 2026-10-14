package com.acme.transport.lastmile.drop.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves drop quotas through their life cycle and keeps a journal of what it did. */
data class DropQuotaCoordinator(
    var store: DropQuotaStore,
    var validator: DropQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DropQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("drop quota $code not found")
        validator.requireValid(item)
        item.moveTo(DropQuotaStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("drop quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DropQuotaStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("drop quota $code not found")
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

fun newDropQuotaCoordinator(store: DropQuotaStore, clock: Clock, flags: Registry): DropQuotaCoordinator {
    return DropQuotaCoordinator(
        store = store,
        validator = defaultDropQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDropQuotaEngine(flags),
    )
}
