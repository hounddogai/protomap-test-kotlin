package com.acme.transport.crewing.qualification.bundle

import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves global qualification bundles through their life cycle and keeps a journal of what it did. */
data class GlobalQualificationBundleWorkflow(
    var store: GlobalQualificationBundleStore,
    var validator: GlobalQualificationBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalQualificationBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("global qualification bundle $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalQualificationBundleStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("global qualification bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalQualificationBundleStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("global qualification bundle $code not found")
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

fun newGlobalQualificationBundleWorkflow(store: GlobalQualificationBundleStore, clock: Clock, flags: Registry): GlobalQualificationBundleWorkflow {
    return GlobalQualificationBundleWorkflow(
        store = store,
        validator = defaultGlobalQualificationBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalQualificationBundleEngine(flags),
    )
}
