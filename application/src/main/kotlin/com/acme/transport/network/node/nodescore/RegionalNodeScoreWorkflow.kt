package com.acme.transport.network.node.nodescore

import com.acme.transport.network.clock.Clock

/** Moves regional node scores through their life cycle and keeps a journal of what it did. */
data class RegionalNodeScoreDesk(
    var store: RegionalNodeScoreStore,
    var validator: RegionalNodeScoreValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional node score $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalNodeScoreStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("regional node score $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalNodeScoreStatus.RETIRED)
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

fun newRegionalNodeScoreDesk(store: RegionalNodeScoreStore, clock: Clock): RegionalNodeScoreDesk {
    return RegionalNodeScoreDesk(
        store = store,
        validator = defaultRegionalNodeScoreValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
