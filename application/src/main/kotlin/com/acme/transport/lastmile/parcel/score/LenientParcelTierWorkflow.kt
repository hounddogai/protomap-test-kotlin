package com.acme.transport.lastmile.parcel.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves lenient parcel tiers through their life cycle and keeps a journal of what it did. */
data class LenientParcelTierService(
    var store: LenientParcelTierStore,
    var validator: LenientParcelTierValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: LenientParcelTierEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("lenient parcel tier $code not found")
        validator.requireValid(item)
        item.moveTo(LenientParcelTierStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient parcel tier $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientParcelTierStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("lenient parcel tier $code not found")
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

fun newLenientParcelTierService(store: LenientParcelTierStore, clock: Clock, flags: Registry): LenientParcelTierService {
    return LenientParcelTierService(
        store = store,
        validator = defaultLenientParcelTierValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultLenientParcelTierEngine(flags),
    )
}
