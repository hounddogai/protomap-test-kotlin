package com.acme.transport.lastmile.parcel.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves fallback parcel targets through their life cycle and keeps a journal of what it did. */
data class FallbackParcelTargetDesk(
    var store: FallbackParcelTargetStore,
    var validator: FallbackParcelTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FallbackParcelTargetEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fallback parcel target $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackParcelTargetStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback parcel target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackParcelTargetStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("fallback parcel target $code not found")
        val result = engine.run(item)
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

fun newFallbackParcelTargetDesk(store: FallbackParcelTargetStore, clock: Clock, flags: Registry): FallbackParcelTargetDesk {
    return FallbackParcelTargetDesk(
        store = store,
        validator = defaultFallbackParcelTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFallbackParcelTargetEngine(flags),
    )
}
