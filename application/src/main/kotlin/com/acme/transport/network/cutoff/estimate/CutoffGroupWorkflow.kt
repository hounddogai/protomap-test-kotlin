package com.acme.transport.network.cutoff.estimate

import com.acme.transport.network.clock.Clock

/** Moves cutoff groups through their life cycle and keeps a journal of what it did. */
data class CutoffGroupService(
    var store: CutoffGroupStore,
    var validator: CutoffGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CutoffGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("cutoff group $code not found")
        validator.requireValid(item)
        item.moveTo(CutoffGroupStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("cutoff group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CutoffGroupStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("cutoff group $code not found")
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

fun newCutoffGroupService(store: CutoffGroupStore, clock: Clock): CutoffGroupService {
    return CutoffGroupService(
        store = store,
        validator = defaultCutoffGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCutoffGroupEngine(),
    )
}
