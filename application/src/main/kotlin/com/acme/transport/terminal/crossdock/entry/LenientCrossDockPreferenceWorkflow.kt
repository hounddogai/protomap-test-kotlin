package com.acme.transport.terminal.crossdock.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.terminal.clock.Clock

/**
 * Moves lenient cross dock preferences through their life cycle and keeps a journal of what it did.
 */
data class LenientCrossDockPreferenceWorkflow(
    var store: LenientCrossDockPreferenceStore,
    var validator: LenientCrossDockPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientCrossDockPreferenceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient cross dock preference $code not found")
        validator.requireValid(item)
        item.moveTo(LenientCrossDockPreferenceStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient cross dock preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientCrossDockPreferenceStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("lenient cross dock preference $code not found")
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

fun newLenientCrossDockPreferenceWorkflow(store: LenientCrossDockPreferenceStore, clock: Clock, flags: Registry): LenientCrossDockPreferenceWorkflow {
    return LenientCrossDockPreferenceWorkflow(
        store = store,
        validator = defaultLenientCrossDockPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientCrossDockPreferenceEngine(flags),
    )
}
