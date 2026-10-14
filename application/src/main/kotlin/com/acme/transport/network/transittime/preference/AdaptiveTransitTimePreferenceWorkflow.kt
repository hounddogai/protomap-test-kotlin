package com.acme.transport.network.transittime.preference

import com.acme.transport.network.clock.Clock

/**
 * Moves adaptive transit time preferences through their life cycle and keeps a journal of what it did.
 */
data class AdaptiveTransitTimePreferenceWorkflow(
    var store: AdaptiveTransitTimePreferenceStore,
    var validator: AdaptiveTransitTimePreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("adaptive transit time preference $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveTransitTimePreferenceStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive transit time preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveTransitTimePreferenceStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newAdaptiveTransitTimePreferenceWorkflow(store: AdaptiveTransitTimePreferenceStore, clock: Clock): AdaptiveTransitTimePreferenceWorkflow {
    return AdaptiveTransitTimePreferenceWorkflow(
        store = store,
        validator = defaultAdaptiveTransitTimePreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
