package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.safety.clock.Clock

/** Moves capped camera event entries through their life cycle and keeps a journal of what it did. */
data class CappedCameraEventEntryWorkflow(
    var store: CappedCameraEventEntryStore,
    var validator: CappedCameraEventEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedCameraEventEntryEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped camera event entry $code not found")
        validator.requireValid(item)
        item.moveTo(CappedCameraEventEntryStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("capped camera event entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedCameraEventEntryStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("capped camera event entry $code not found")
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

fun newCappedCameraEventEntryWorkflow(store: CappedCameraEventEntryStore, clock: Clock): CappedCameraEventEntryWorkflow {
    return CappedCameraEventEntryWorkflow(
        store = store,
        validator = defaultCappedCameraEventEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedCameraEventEntryEngine(),
    )
}
