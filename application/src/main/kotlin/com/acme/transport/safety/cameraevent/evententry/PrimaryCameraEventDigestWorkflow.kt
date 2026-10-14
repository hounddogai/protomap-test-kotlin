package com.acme.transport.safety.cameraevent.evententry

import com.acme.transport.safety.clock.Clock

/** Moves primary camera event digests through their life cycle and keeps a journal of what it did. */
data class PrimaryCameraEventDigestManager(
    var store: PrimaryCameraEventDigestStore,
    var validator: PrimaryCameraEventDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryCameraEventDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("primary camera event digest $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryCameraEventDigestStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("primary camera event digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryCameraEventDigestStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("primary camera event digest $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newPrimaryCameraEventDigestManager(store: PrimaryCameraEventDigestStore, clock: Clock): PrimaryCameraEventDigestManager {
    return PrimaryCameraEventDigestManager(
        store = store,
        validator = defaultPrimaryCameraEventDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryCameraEventDigestEngine(),
    )
}
