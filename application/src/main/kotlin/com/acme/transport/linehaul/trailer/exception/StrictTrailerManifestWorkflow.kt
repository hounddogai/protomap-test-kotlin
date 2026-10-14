package com.acme.transport.linehaul.trailer.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves strict trailer manifests through their life cycle and keeps a journal of what it did. */
data class StrictTrailerManifestService(
    var store: StrictTrailerManifestStore,
    var validator: StrictTrailerManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: StrictTrailerManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("strict trailer manifest $code not found")
        validator.requireValid(item)
        item.moveTo(StrictTrailerManifestStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("strict trailer manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictTrailerManifestStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("strict trailer manifest $code not found")
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

fun newStrictTrailerManifestService(store: StrictTrailerManifestStore, clock: Clock, flags: Registry): StrictTrailerManifestService {
    return StrictTrailerManifestService(
        store = store,
        validator = defaultStrictTrailerManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultStrictTrailerManifestEngine(flags),
    )
}
