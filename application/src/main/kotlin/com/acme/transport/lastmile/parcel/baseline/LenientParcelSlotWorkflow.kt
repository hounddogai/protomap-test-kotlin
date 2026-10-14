package com.acme.transport.lastmile.parcel.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves lenient parcel slots through their life cycle and keeps a journal of what it did. */
data class LenientParcelSlotWorkflow(
    var store: LenientParcelSlotStore,
    var validator: LenientParcelSlotValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientParcelSlotEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lenient parcel slot $code not found")
        validator.requireValid(item)
        item.moveTo(LenientParcelSlotStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient parcel slot $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientParcelSlotStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("lenient parcel slot $code not found")
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
            if (item.updatedAt > cutoff) {
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

fun newLenientParcelSlotWorkflow(store: LenientParcelSlotStore, clock: Clock, flags: Registry): LenientParcelSlotWorkflow {
    return LenientParcelSlotWorkflow(
        store = store,
        validator = defaultLenientParcelSlotValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientParcelSlotEngine(flags),
    )
}
