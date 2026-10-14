package com.acme.transport.linehaul.laneplan.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves legacy lane plan bundles through their life cycle and keeps a journal of what it did. */
data class LegacyLanePlanBundleManager(
    var store: LegacyLanePlanBundleStore,
    var validator: LegacyLanePlanBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LegacyLanePlanBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("legacy lane plan bundle $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyLanePlanBundleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy lane plan bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyLanePlanBundleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("legacy lane plan bundle $code not found")
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

fun newLegacyLanePlanBundleManager(store: LegacyLanePlanBundleStore, clock: Clock, flags: Registry): LegacyLanePlanBundleManager {
    return LegacyLanePlanBundleManager(
        store = store,
        validator = defaultLegacyLanePlanBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLegacyLanePlanBundleEngine(flags),
    )
}
