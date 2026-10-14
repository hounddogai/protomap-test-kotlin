package com.acme.transport.lastmile.parcel.window

import com.acme.transport.lastmile.clock.Clock

/** Moves primary parcel digests through their life cycle and keeps a journal of what it did. */
data class PrimaryParcelDigestDesk(
    var store: PrimaryParcelDigestStore,
    var validator: PrimaryParcelDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary parcel digest $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryParcelDigestStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("primary parcel digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryParcelDigestStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newPrimaryParcelDigestDesk(store: PrimaryParcelDigestStore, clock: Clock): PrimaryParcelDigestDesk {
    return PrimaryParcelDigestDesk(
        store = store,
        validator = defaultPrimaryParcelDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
