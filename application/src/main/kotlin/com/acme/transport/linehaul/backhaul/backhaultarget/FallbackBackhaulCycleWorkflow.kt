package com.acme.transport.linehaul.backhaul.backhaultarget

import com.acme.transport.linehaul.clock.Clock

/** Moves fallback backhaul cycles through their life cycle and keeps a journal of what it did. */
data class FallbackBackhaulCycleWorkflow(
    var store: FallbackBackhaulCycleStore,
    var validator: FallbackBackhaulCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback backhaul cycle $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackBackhaulCycleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback backhaul cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackBackhaulCycleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newFallbackBackhaulCycleWorkflow(store: FallbackBackhaulCycleStore, clock: Clock): FallbackBackhaulCycleWorkflow {
    return FallbackBackhaulCycleWorkflow(
        store = store,
        validator = defaultFallbackBackhaulCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
