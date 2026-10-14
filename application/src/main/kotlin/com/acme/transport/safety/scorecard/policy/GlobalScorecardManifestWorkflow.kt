package com.acme.transport.safety.scorecard.policy

import com.acme.transport.safety.clock.Clock

/** Moves global scorecard manifests through their life cycle and keeps a journal of what it did. */
data class GlobalScorecardManifestCoordinator(
    var store: GlobalScorecardManifestStore,
    var validator: GlobalScorecardManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalScorecardManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("global scorecard manifest $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalScorecardManifestStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("global scorecard manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalScorecardManifestStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("global scorecard manifest $code not found")
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

fun newGlobalScorecardManifestCoordinator(store: GlobalScorecardManifestStore, clock: Clock): GlobalScorecardManifestCoordinator {
    return GlobalScorecardManifestCoordinator(
        store = store,
        validator = defaultGlobalScorecardManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalScorecardManifestEngine(),
    )
}
