package com.acme.transport.linehaul.relay.bundle

import com.acme.transport.linehaul.clock.Clock

/** Moves global relay bundles through their life cycle and keeps a journal of what it did. */
data class GlobalRelayBundleDesk(
    var store: GlobalRelayBundleStore,
    var validator: GlobalRelayBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalRelayBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global relay bundle $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalRelayBundleStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("global relay bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalRelayBundleStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("global relay bundle $code not found")
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
            if (item.expiresAt > cutoff) {
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

fun newGlobalRelayBundleDesk(store: GlobalRelayBundleStore, clock: Clock): GlobalRelayBundleDesk {
    return GlobalRelayBundleDesk(
        store = store,
        validator = defaultGlobalRelayBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalRelayBundleEngine(),
    )
}
