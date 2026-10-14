package com.acme.transport.freightpay.accessorial.profile

import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.flags.Registry

/** Moves primary accessorial groups through their life cycle and keeps a journal of what it did. */
data class PrimaryAccessorialGroupCoordinator(
    var store: PrimaryAccessorialGroupStore,
    var validator: PrimaryAccessorialGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryAccessorialGroupEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary accessorial group $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryAccessorialGroupStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary accessorial group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryAccessorialGroupStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("primary accessorial group $code not found")
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

fun newPrimaryAccessorialGroupCoordinator(store: PrimaryAccessorialGroupStore, clock: Clock, flags: Registry): PrimaryAccessorialGroupCoordinator {
    return PrimaryAccessorialGroupCoordinator(
        store = store,
        validator = defaultPrimaryAccessorialGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryAccessorialGroupEngine(flags),
    )
}
