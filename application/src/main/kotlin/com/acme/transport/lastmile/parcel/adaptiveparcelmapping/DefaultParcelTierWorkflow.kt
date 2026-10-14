package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves default parcel tiers through their life cycle and keeps a journal of what it did. */
data class DefaultParcelTierService(
    var store: DefaultParcelTierStore,
    var validator: DefaultParcelTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: DefaultParcelTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("default parcel tier $code not found")
        validator.requireValid(item)
        item.moveTo(DefaultParcelTierStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("default parcel tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(DefaultParcelTierStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("default parcel tier $code not found")
        val result = engine.evaluate(item)
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

fun newDefaultParcelTierService(store: DefaultParcelTierStore, clock: Clock, flags: Registry): DefaultParcelTierService {
    return DefaultParcelTierService(
        store = store,
        validator = defaultDefaultParcelTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultDefaultParcelTierEngine(flags),
    )
}
