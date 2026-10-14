package com.acme.transport.safety.nearmiss.window

import com.acme.transport.safety.clock.Clock

/** Moves near miss quotas through their life cycle and keeps a journal of what it did. */
data class NearMissQuotaCoordinator(
    var store: NearMissQuotaStore,
    var validator: NearMissQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: NearMissQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("near miss quota $code not found")
        validator.requireValid(item)
        item.moveTo(NearMissQuotaStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("near miss quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(NearMissQuotaStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("near miss quota $code not found")
        val result = engine.price(item)
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

fun newNearMissQuotaCoordinator(store: NearMissQuotaStore, clock: Clock): NearMissQuotaCoordinator {
    return NearMissQuotaCoordinator(
        store = store,
        validator = defaultNearMissQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultNearMissQuotaEngine(),
    )
}
