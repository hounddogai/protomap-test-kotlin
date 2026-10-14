package com.acme.transport.emissions.evcharge.proposal

import com.acme.transport.emissions.clock.Clock

/** Moves ev charge overrides through their life cycle and keeps a journal of what it did. */
data class EvChargeOverrideWorkflow(
    var store: EvChargeOverrideStore,
    var validator: EvChargeOverrideValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: EvChargeOverrideEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("ev charge override $code not found")
        validator.requireValid(item)
        item.moveTo(EvChargeOverrideStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("ev charge override $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(EvChargeOverrideStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("ev charge override $code not found")
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
            if (item.endsAt > cutoff) {
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

fun newEvChargeOverrideWorkflow(store: EvChargeOverrideStore, clock: Clock): EvChargeOverrideWorkflow {
    return EvChargeOverrideWorkflow(
        store = store,
        validator = defaultEvChargeOverrideValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultEvChargeOverrideEngine(),
    )
}
