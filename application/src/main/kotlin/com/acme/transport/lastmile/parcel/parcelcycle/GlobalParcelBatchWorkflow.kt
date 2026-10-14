package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves global parcel batches through their life cycle and keeps a journal of what it did. */
data class GlobalParcelBatchCoordinator(
    var store: GlobalParcelBatchStore,
    var validator: GlobalParcelBatchValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalParcelBatchEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global parcel batch $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalParcelBatchStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("global parcel batch $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalParcelBatchStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("global parcel batch $code not found")
        val result = engine.evaluate(item)
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

fun newGlobalParcelBatchCoordinator(store: GlobalParcelBatchStore, clock: Clock, flags: Registry): GlobalParcelBatchCoordinator {
    return GlobalParcelBatchCoordinator(
        store = store,
        validator = defaultGlobalParcelBatchValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalParcelBatchEngine(flags),
    )
}
