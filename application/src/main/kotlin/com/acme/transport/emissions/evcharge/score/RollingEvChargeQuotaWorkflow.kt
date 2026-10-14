package com.acme.transport.emissions.evcharge.score

import com.acme.transport.emissions.clock.Clock

/** Moves rolling ev charge quotas through their life cycle and keeps a journal of what it did. */
data class RollingEvChargeQuotaCoordinator(
    var store: RollingEvChargeQuotaStore,
    var validator: RollingEvChargeQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingEvChargeQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling ev charge quota $code not found")
        validator.requireValid(item)
        item.moveTo(RollingEvChargeQuotaStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling ev charge quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingEvChargeQuotaStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun evaluate(code: String): Double {
        val item = store.find(code) ?: error("rolling ev charge quota $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newRollingEvChargeQuotaCoordinator(store: RollingEvChargeQuotaStore, clock: Clock): RollingEvChargeQuotaCoordinator {
    return RollingEvChargeQuotaCoordinator(
        store = store,
        validator = defaultRollingEvChargeQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingEvChargeQuotaEngine(),
    )
}
