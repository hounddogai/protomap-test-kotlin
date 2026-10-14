package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.lastmile.clock.Clock

/** Moves drop variances through their life cycle and keeps a journal of what it did. */
data class DropVarianceService(
    var store: DropVarianceStore,
    var validator: DropVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DropVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("drop variance $code not found")
        validator.requireValid(item)
        item.moveTo(DropVarianceStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("drop variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DropVarianceStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("drop variance $code not found")
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

fun newDropVarianceService(store: DropVarianceStore, clock: Clock): DropVarianceService {
    return DropVarianceService(
        store = store,
        validator = defaultDropVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDropVarianceEngine(),
    )
}
