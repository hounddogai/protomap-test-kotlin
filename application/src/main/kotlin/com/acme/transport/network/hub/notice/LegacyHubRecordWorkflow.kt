package com.acme.transport.network.hub.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves legacy hub records through their life cycle and keeps a journal of what it did. */
data class LegacyHubRecordCoordinator(
    var store: LegacyHubRecordStore,
    var validator: LegacyHubRecordValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyHubRecordEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("legacy hub record $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyHubRecordStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy hub record $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyHubRecordStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("legacy hub record $code not found")
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

fun newLegacyHubRecordCoordinator(store: LegacyHubRecordStore, clock: Clock, flags: Registry): LegacyHubRecordCoordinator {
    return LegacyHubRecordCoordinator(
        store = store,
        validator = defaultLegacyHubRecordValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyHubRecordEngine(flags),
    )
}
