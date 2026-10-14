package com.acme.transport.terminal.dwell.preference

import com.acme.transport.terminal.clock.Clock

/** Moves dwell preferences through their life cycle and keeps a journal of what it did. */
data class DwellPreferenceWorkflow(
    var store: DwellPreferenceStore,
    var validator: DwellPreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("dwell preference $code not found")
        validator.requireValid(item)
        item.moveTo(DwellPreferenceStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("dwell preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DwellPreferenceStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
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

fun newDwellPreferenceWorkflow(store: DwellPreferenceStore, clock: Clock): DwellPreferenceWorkflow {
    return DwellPreferenceWorkflow(
        store = store,
        validator = defaultDwellPreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
