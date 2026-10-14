package com.acme.transport.customs.broker.exception

import com.acme.transport.customs.clock.Clock

/** Moves regional broker variances through their life cycle and keeps a journal of what it did. */
data class RegionalBrokerVarianceCoordinator(
    var store: RegionalBrokerVarianceStore,
    var validator: RegionalBrokerVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("regional broker variance $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalBrokerVarianceStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("regional broker variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalBrokerVarianceStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.expiresAt > cutoff) {
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

fun newRegionalBrokerVarianceCoordinator(store: RegionalBrokerVarianceStore, clock: Clock): RegionalBrokerVarianceCoordinator {
    return RegionalBrokerVarianceCoordinator(
        store = store,
        validator = defaultRegionalBrokerVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
