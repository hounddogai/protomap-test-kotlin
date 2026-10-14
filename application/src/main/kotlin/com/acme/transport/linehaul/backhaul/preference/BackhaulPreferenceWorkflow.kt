package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.linehaul.clock.Clock

/** Moves backhaul preferences through their life cycle and keeps a journal of what it did. */
data class BackhaulPreferenceService(
    var store: BackhaulPreferenceStore,
    var validator: BackhaulPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: BackhaulPreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("backhaul preference $code not found")
        validator.requireValid(item)
        item.moveTo(BackhaulPreferenceStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("backhaul preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(BackhaulPreferenceStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("backhaul preference $code not found")
        val result = engine.compute(item)
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

fun newBackhaulPreferenceService(store: BackhaulPreferenceStore, clock: Clock): BackhaulPreferenceService {
    return BackhaulPreferenceService(
        store = store,
        validator = defaultBackhaulPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultBackhaulPreferenceEngine(),
    )
}
