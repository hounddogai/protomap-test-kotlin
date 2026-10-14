package com.acme.transport.emissions.idling.manifest

import com.acme.transport.emissions.clock.Clock

/** Moves default idling segments through their life cycle and keeps a journal of what it did. */
data class DefaultIdlingSegmentDesk(
    var store: DefaultIdlingSegmentStore,
    var validator: DefaultIdlingSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultIdlingSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("default idling segment $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultIdlingSegmentStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("default idling segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultIdlingSegmentStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("default idling segment $code not found")
        val result = engine.run(item)
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

fun newDefaultIdlingSegmentDesk(store: DefaultIdlingSegmentStore, clock: Clock): DefaultIdlingSegmentDesk {
    return DefaultIdlingSegmentDesk(
        store = store,
        validator = defaultDefaultIdlingSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultIdlingSegmentEngine(),
    )
}
