package com.acme.transport.customs.clearance.clearancemanifest

import com.acme.transport.customs.clock.Clock

/** Moves capped clearance bundles through their life cycle and keeps a journal of what it did. */
data class CappedClearanceBundleWorkflow(
    var store: CappedClearanceBundleStore,
    var validator: CappedClearanceBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedClearanceBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capped clearance bundle $code not found")
        validator.requireValid(item)
        item.moveTo(CappedClearanceBundleStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("capped clearance bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedClearanceBundleStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capped clearance bundle $code not found")
        val result = engine.evaluate(item)
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

fun newCappedClearanceBundleWorkflow(store: CappedClearanceBundleStore, clock: Clock): CappedClearanceBundleWorkflow {
    return CappedClearanceBundleWorkflow(
        store = store,
        validator = defaultCappedClearanceBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedClearanceBundleEngine(),
    )
}
