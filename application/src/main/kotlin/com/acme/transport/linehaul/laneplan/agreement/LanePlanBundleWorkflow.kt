package com.acme.transport.linehaul.laneplan.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.linehaul.clock.Clock

/** Moves lane plan bundles through their life cycle and keeps a journal of what it did. */
data class LanePlanBundleWorkflow(
    var store: LanePlanBundleStore,
    var validator: LanePlanBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LanePlanBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("lane plan bundle $code not found")
        validator.requireValid(item)
        item.moveTo(LanePlanBundleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lane plan bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LanePlanBundleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("lane plan bundle $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newLanePlanBundleWorkflow(store: LanePlanBundleStore, clock: Clock, flags: Registry): LanePlanBundleWorkflow {
    return LanePlanBundleWorkflow(
        store = store,
        validator = defaultLanePlanBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLanePlanBundleEngine(flags),
    )
}
