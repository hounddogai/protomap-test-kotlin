package com.acme.transport.linehaul.trailer.policy

import com.acme.transport.linehaul.clock.Clock

/** Moves trailer adjustments through their life cycle and keeps a journal of what it did. */
data class TrailerAdjustmentService(
    var store: TrailerAdjustmentStore,
    var validator: TrailerAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: TrailerAdjustmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("trailer adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(TrailerAdjustmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("trailer adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TrailerAdjustmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("trailer adjustment $code not found")
        val result = engine.run(item)
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

fun newTrailerAdjustmentService(store: TrailerAdjustmentStore, clock: Clock): TrailerAdjustmentService {
    return TrailerAdjustmentService(
        store = store,
        validator = defaultTrailerAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultTrailerAdjustmentEngine(),
    )
}
