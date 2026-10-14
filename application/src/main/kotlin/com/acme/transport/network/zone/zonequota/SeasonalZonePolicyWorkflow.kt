package com.acme.transport.network.zone.zonequota

import com.acme.transport.network.clock.Clock

/** Moves seasonal zone policies through their life cycle and keeps a journal of what it did. */
data class SeasonalZonePolicyManager(
    var store: SeasonalZonePolicyStore,
    var validator: SeasonalZonePolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("seasonal zone policy $code not found")
        validator.requireValid(item)
        item.moveTo(SeasonalZonePolicyStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("seasonal zone policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(SeasonalZonePolicyStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newSeasonalZonePolicyManager(store: SeasonalZonePolicyStore, clock: Clock): SeasonalZonePolicyManager {
    return SeasonalZonePolicyManager(
        store = store,
        validator = defaultSeasonalZonePolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
