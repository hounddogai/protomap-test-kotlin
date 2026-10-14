package com.acme.transport.lastmile.parcel.parcelslot

import com.acme.transport.lastmile.clock.Clock

/** Moves scheduled parcel digests through their life cycle and keeps a journal of what it did. */
data class ScheduledParcelDigestWorkflow(
    var store: ScheduledParcelDigestStore,
    var validator: ScheduledParcelDigestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ScheduledParcelDigestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("scheduled parcel digest $code not found")
        validator.requireValid(item)
        item.moveTo(ScheduledParcelDigestStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("scheduled parcel digest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ScheduledParcelDigestStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun score(code: String): Double {
        val item = store.find(code) ?: error("scheduled parcel digest $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newScheduledParcelDigestWorkflow(store: ScheduledParcelDigestStore, clock: Clock): ScheduledParcelDigestWorkflow {
    return ScheduledParcelDigestWorkflow(
        store = store,
        validator = defaultScheduledParcelDigestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultScheduledParcelDigestEngine(),
    )
}
