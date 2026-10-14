package com.acme.transport.emissions.idling.bundle

import com.acme.transport.emissions.clock.Clock

/** Moves primary idling bundles through their life cycle and keeps a journal of what it did. */
data class PrimaryIdlingBundleCoordinator(
    var store: PrimaryIdlingBundleStore,
    var validator: PrimaryIdlingBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("primary idling bundle $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryIdlingBundleStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("primary idling bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryIdlingBundleStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.startsAt > cutoff) {
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

fun newPrimaryIdlingBundleCoordinator(store: PrimaryIdlingBundleStore, clock: Clock): PrimaryIdlingBundleCoordinator {
    return PrimaryIdlingBundleCoordinator(
        store = store,
        validator = defaultPrimaryIdlingBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
