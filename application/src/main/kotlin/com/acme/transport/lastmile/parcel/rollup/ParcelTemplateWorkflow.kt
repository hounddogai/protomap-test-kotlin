package com.acme.transport.lastmile.parcel.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves parcel templates through their life cycle and keeps a journal of what it did. */
data class ParcelTemplateManager(
    var store: ParcelTemplateStore,
    var validator: ParcelTemplateValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ParcelTemplateEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("parcel template $code not found")
        validator.requireValid(item)
        item.moveTo(ParcelTemplateStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("parcel template $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ParcelTemplateStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("parcel template $code not found")
        val result = engine.evaluate(item)
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

fun newParcelTemplateManager(store: ParcelTemplateStore, clock: Clock, flags: Registry): ParcelTemplateManager {
    return ParcelTemplateManager(
        store = store,
        validator = defaultParcelTemplateValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultParcelTemplateEngine(flags),
    )
}
