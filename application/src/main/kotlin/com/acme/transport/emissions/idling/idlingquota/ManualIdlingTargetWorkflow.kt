package com.acme.transport.emissions.idling.idlingquota

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves manual idling targets through their life cycle and keeps a journal of what it did. */
data class ManualIdlingTargetDesk(
    var store: ManualIdlingTargetStore,
    var validator: ManualIdlingTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualIdlingTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("manual idling target $code not found")
        validator.requireValid(item)
        item.moveTo(ManualIdlingTargetStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("manual idling target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualIdlingTargetStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("manual idling target $code not found")
        val result = engine.evaluate(item)
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

fun newManualIdlingTargetDesk(store: ManualIdlingTargetStore, clock: Clock, flags: Registry): ManualIdlingTargetDesk {
    return ManualIdlingTargetDesk(
        store = store,
        validator = defaultManualIdlingTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualIdlingTargetEngine(flags),
    )
}
