package com.acme.transport.terminal.crossdock.schedule

import com.acme.transport.terminal.clock.Clock

/** Moves cross dock digests through their life cycle and keeps a journal of what it did. */
data class CrossDockDigestManager(
    var store: CrossDockDigestStore,
    var validator: CrossDockDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CrossDockDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("cross dock digest $code not found")
        validator.requireValid(item)
        item.moveTo(CrossDockDigestStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("cross dock digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CrossDockDigestStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("cross dock digest $code not found")
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

fun newCrossDockDigestManager(store: CrossDockDigestStore, clock: Clock): CrossDockDigestManager {
    return CrossDockDigestManager(
        store = store,
        validator = defaultCrossDockDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCrossDockDigestEngine(),
    )
}
