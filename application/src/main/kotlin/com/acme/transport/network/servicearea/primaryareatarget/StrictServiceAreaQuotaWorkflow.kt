package com.acme.transport.network.servicearea.primaryareatarget

import com.acme.transport.network.clock.Clock

/** Moves strict service area quotas through their life cycle and keeps a journal of what it did. */
data class StrictServiceAreaQuotaService(
    var store: StrictServiceAreaQuotaStore,
    var validator: StrictServiceAreaQuotaValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("strict service area quota $code not found")
        validator.requireValid(item)
        item.moveTo(StrictServiceAreaQuotaStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("strict service area quota $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictServiceAreaQuotaStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newStrictServiceAreaQuotaService(store: StrictServiceAreaQuotaStore, clock: Clock): StrictServiceAreaQuotaService {
    return StrictServiceAreaQuotaService(
        store = store,
        validator = defaultStrictServiceAreaQuotaValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
