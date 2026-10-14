package com.acme.transport.emissions.fuelburn.pendingburnthreshold

import com.acme.transport.emissions.clock.Clock

/** Moves regional fuel burn audits through their life cycle and keeps a journal of what it did. */
data class RegionalFuelBurnAuditCoordinator(
    var store: RegionalFuelBurnAuditStore,
    var validator: RegionalFuelBurnAuditValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RegionalFuelBurnAuditEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("regional fuel burn audit $code not found")
        validator.requireValid(item)
        item.moveTo(RegionalFuelBurnAuditStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("regional fuel burn audit $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RegionalFuelBurnAuditStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("regional fuel burn audit $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newRegionalFuelBurnAuditCoordinator(store: RegionalFuelBurnAuditStore, clock: Clock): RegionalFuelBurnAuditCoordinator {
    return RegionalFuelBurnAuditCoordinator(
        store = store,
        validator = defaultRegionalFuelBurnAuditValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRegionalFuelBurnAuditEngine(),
    )
}
