package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves global service area segments through their life cycle and keeps a journal of what it did. */
data class GlobalServiceAreaSegmentWorkflow(
    var store: GlobalServiceAreaSegmentStore,
    var validator: GlobalServiceAreaSegmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalServiceAreaSegmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global service area segment $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalServiceAreaSegmentStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("global service area segment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalServiceAreaSegmentStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("global service area segment $code not found")
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

fun newGlobalServiceAreaSegmentWorkflow(store: GlobalServiceAreaSegmentStore, clock: Clock, flags: Registry): GlobalServiceAreaSegmentWorkflow {
    return GlobalServiceAreaSegmentWorkflow(
        store = store,
        validator = defaultGlobalServiceAreaSegmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalServiceAreaSegmentEngine(flags),
    )
}
