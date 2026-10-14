package com.acme.transport.lastmile.parcel.parcelbaseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.lastmile.clock.Clock

/** Moves capped parcel quotas through their life cycle and keeps a journal of what it did. */
data class CappedParcelQuotaManager(
    var store: CappedParcelQuotaStore,
    var validator: CappedParcelQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: CappedParcelQuotaEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("capped parcel quota $code not found")
        validator.requireValid(item)
        item.moveTo(CappedParcelQuotaStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("capped parcel quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedParcelQuotaStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("capped parcel quota $code not found")
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

fun newCappedParcelQuotaManager(store: CappedParcelQuotaStore, clock: Clock, flags: Registry): CappedParcelQuotaManager {
    return CappedParcelQuotaManager(
        store = store,
        validator = defaultCappedParcelQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultCappedParcelQuotaEngine(flags),
    )
}
