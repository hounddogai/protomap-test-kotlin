package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves rolling fuel burn agreements through their life cycle and keeps a journal of what it did. */
data class RollingFuelBurnAgreementService(
    var store: RollingFuelBurnAgreementStore,
    var validator: RollingFuelBurnAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: RollingFuelBurnAgreementEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("rolling fuel burn agreement $code not found")
        validator.requireValid(item)
        item.moveTo(RollingFuelBurnAgreementStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("rolling fuel burn agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(RollingFuelBurnAgreementStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("rolling fuel burn agreement $code not found")
        val result = engine.compute(item)
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

fun newRollingFuelBurnAgreementService(store: RollingFuelBurnAgreementStore, clock: Clock, flags: Registry): RollingFuelBurnAgreementService {
    return RollingFuelBurnAgreementService(
        store = store,
        validator = defaultRollingFuelBurnAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultRollingFuelBurnAgreementEngine(flags),
    )
}
