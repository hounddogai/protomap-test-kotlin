package com.acme.transport.emissions.fuelburn.review

import com.acme.transport.emissions.clock.Clock

/** Moves rolling fuel burn quotas through their life cycle and keeps a journal of what it did. */
data class RollingFuelBurnQuotaDesk(
    var store: RollingFuelBurnQuotaStore,
    var validator: RollingFuelBurnQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingFuelBurnQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("rolling fuel burn quota $code not found")
        validator.requireValid(item)
        item.moveTo(RollingFuelBurnQuotaStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling fuel burn quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingFuelBurnQuotaStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("rolling fuel burn quota $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
    }

    fun sweep(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.updatedAt > cutoff) {
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

fun newRollingFuelBurnQuotaDesk(store: RollingFuelBurnQuotaStore, clock: Clock): RollingFuelBurnQuotaDesk {
    return RollingFuelBurnQuotaDesk(
        store = store,
        validator = defaultRollingFuelBurnQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingFuelBurnQuotaEngine(),
    )
}
