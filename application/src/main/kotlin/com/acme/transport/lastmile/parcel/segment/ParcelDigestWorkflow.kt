package com.acme.transport.lastmile.parcel.segment

import com.acme.transport.lastmile.clock.Clock

/** Moves parcel digests through their life cycle and keeps a journal of what it did. */
data class ParcelDigestWorkflow(
    var store: ParcelDigestStore,
    var validator: ParcelDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("parcel digest $code not found")
        validator.requireValid(item)
        item.moveTo(ParcelDigestStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("parcel digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ParcelDigestStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newParcelDigestWorkflow(store: ParcelDigestStore, clock: Clock): ParcelDigestWorkflow {
    return ParcelDigestWorkflow(
        store = store,
        validator = defaultParcelDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
