package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves lenient cutoff manifests through their life cycle and keeps a journal of what it did. */
data class LenientCutoffManifestDesk(
    var store: LenientCutoffManifestStore,
    var validator: LenientCutoffManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientCutoffManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient cutoff manifest $code not found")
        validator.requireValid(item)
        item.moveTo(LenientCutoffManifestStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient cutoff manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientCutoffManifestStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("lenient cutoff manifest $code not found")
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

fun newLenientCutoffManifestDesk(store: LenientCutoffManifestStore, clock: Clock, flags: Registry): LenientCutoffManifestDesk {
    return LenientCutoffManifestDesk(
        store = store,
        validator = defaultLenientCutoffManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientCutoffManifestEngine(flags),
    )
}
