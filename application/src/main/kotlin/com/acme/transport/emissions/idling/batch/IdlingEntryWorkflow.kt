package com.acme.transport.emissions.idling.batch

import com.acme.transport.emissions.clock.Clock

/** Moves idling entries through their life cycle and keeps a journal of what it did. */
data class IdlingEntryManager(
    var store: IdlingEntryStore,
    var validator: IdlingEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: IdlingEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("idling entry $code not found")
        validator.requireValid(item)
        item.moveTo(IdlingEntryStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("idling entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(IdlingEntryStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("idling entry $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun scan(cutoff: Int): Int {
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

fun newIdlingEntryManager(store: IdlingEntryStore, clock: Clock): IdlingEntryManager {
    return IdlingEntryManager(
        store = store,
        validator = defaultIdlingEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultIdlingEntryEngine(),
    )
}
