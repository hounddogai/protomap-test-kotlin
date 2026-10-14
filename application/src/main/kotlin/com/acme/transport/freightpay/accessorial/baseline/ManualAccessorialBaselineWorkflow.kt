package com.acme.transport.freightpay.accessorial.baseline

import com.acme.transport.freightpay.clock.Clock

/** Moves manual accessorial baselines through their life cycle and keeps a journal of what it did. */
data class ManualAccessorialBaselineDesk(
    var store: ManualAccessorialBaselineStore,
    var validator: ManualAccessorialBaselineValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("manual accessorial baseline $code not found")
        validator.requireValid(item)
        item.moveTo(ManualAccessorialBaselineStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("manual accessorial baseline $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualAccessorialBaselineStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
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

fun newManualAccessorialBaselineDesk(store: ManualAccessorialBaselineStore, clock: Clock): ManualAccessorialBaselineDesk {
    return ManualAccessorialBaselineDesk(
        store = store,
        validator = defaultManualAccessorialBaselineValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
