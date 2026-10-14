package com.acme.transport.linehaul.tender.weightedtenderrollup

import com.acme.transport.linehaul.clock.Clock

/** Moves pending tender plans through their life cycle and keeps a journal of what it did. */
data class PendingTenderPlanManager(
    var store: PendingTenderPlanStore,
    var validator: PendingTenderPlanValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PendingTenderPlanEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("pending tender plan $code not found")
        validator.requireValid(item)
        item.moveTo(PendingTenderPlanStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("pending tender plan $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PendingTenderPlanStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("pending tender plan $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newPendingTenderPlanManager(store: PendingTenderPlanStore, clock: Clock): PendingTenderPlanManager {
    return PendingTenderPlanManager(
        store = store,
        validator = defaultPendingTenderPlanValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPendingTenderPlanEngine(),
    )
}
