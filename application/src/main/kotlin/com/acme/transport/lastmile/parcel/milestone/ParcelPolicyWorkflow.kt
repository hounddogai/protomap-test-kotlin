package com.acme.transport.lastmile.parcel.milestone

import com.acme.transport.lastmile.clock.Clock

/** Moves parcel policies through their life cycle and keeps a journal of what it did. */
data class ParcelPolicyDesk(
    var store: ParcelPolicyStore,
    var validator: ParcelPolicyValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ParcelPolicyEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("parcel policy $code not found")
        validator.requireValid(item)
        item.moveTo(ParcelPolicyStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("parcel policy $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ParcelPolicyStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("parcel policy $code not found")
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

fun newParcelPolicyDesk(store: ParcelPolicyStore, clock: Clock): ParcelPolicyDesk {
    return ParcelPolicyDesk(
        store = store,
        validator = defaultParcelPolicyValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultParcelPolicyEngine(),
    )
}
