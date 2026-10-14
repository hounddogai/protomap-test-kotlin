package com.acme.transport.emissions.idling.idlingmanifest

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves lenient idling batches through their life cycle and keeps a journal of what it did. */
data class LenientIdlingBatchCoordinator(
    var store: LenientIdlingBatchStore,
    var validator: LenientIdlingBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientIdlingBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lenient idling batch $code not found")
        validator.requireValid(item)
        item.moveTo(LenientIdlingBatchStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient idling batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientIdlingBatchStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lenient idling batch $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
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

fun newLenientIdlingBatchCoordinator(store: LenientIdlingBatchStore, clock: Clock, flags: Registry): LenientIdlingBatchCoordinator {
    return LenientIdlingBatchCoordinator(
        store = store,
        validator = defaultLenientIdlingBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientIdlingBatchEngine(flags),
    )
}
