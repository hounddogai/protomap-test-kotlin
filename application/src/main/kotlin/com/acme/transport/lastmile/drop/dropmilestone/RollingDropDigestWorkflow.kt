package com.acme.transport.lastmile.drop.dropmilestone

import com.acme.transport.lastmile.clock.Clock

/** Moves rolling drop digests through their life cycle and keeps a journal of what it did. */
data class RollingDropDigestCoordinator(
    var store: RollingDropDigestStore,
    var validator: RollingDropDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingDropDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("rolling drop digest $code not found")
        validator.requireValid(item)
        item.moveTo(RollingDropDigestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling drop digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingDropDigestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("rolling drop digest $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newRollingDropDigestCoordinator(store: RollingDropDigestStore, clock: Clock): RollingDropDigestCoordinator {
    return RollingDropDigestCoordinator(
        store = store,
        validator = defaultRollingDropDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingDropDigestEngine(),
    )
}
