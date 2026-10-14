package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.emissions.clock.Clock

/** Moves global fuel burn assignments through their life cycle and keeps a journal of what it did. */
data class GlobalFuelBurnAssignmentManager(
    var store: GlobalFuelBurnAssignmentStore,
    var validator: GlobalFuelBurnAssignmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: GlobalFuelBurnAssignmentEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("global fuel burn assignment $code not found")
        validator.requireValid(item)
        item.moveTo(GlobalFuelBurnAssignmentStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("global fuel burn assignment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(GlobalFuelBurnAssignmentStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("global fuel burn assignment $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newGlobalFuelBurnAssignmentManager(store: GlobalFuelBurnAssignmentStore, clock: Clock): GlobalFuelBurnAssignmentManager {
    return GlobalFuelBurnAssignmentManager(
        store = store,
        validator = defaultGlobalFuelBurnAssignmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultGlobalFuelBurnAssignmentEngine(),
    )
}
