package com.acme.transport.network.node.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.network.clock.Clock

/** Moves lenient node manifests through their life cycle and keeps a journal of what it did. */
data class LenientNodeManifestDesk(
    var store: LenientNodeManifestStore,
    var validator: LenientNodeManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientNodeManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient node manifest $code not found")
        validator.requireValid(item)
        item.moveTo(LenientNodeManifestStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient node manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientNodeManifestStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("lenient node manifest $code not found")
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

fun newLenientNodeManifestDesk(store: LenientNodeManifestStore, clock: Clock, flags: Registry): LenientNodeManifestDesk {
    return LenientNodeManifestDesk(
        store = store,
        validator = defaultLenientNodeManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientNodeManifestEngine(flags),
    )
}
