package com.acme.transport.network.servicearea.request

import com.acme.transport.network.clock.Clock

/** Moves service area entries through their life cycle and keeps a journal of what it did. */
data class ServiceAreaEntryService(
    var store: ServiceAreaEntryStore,
    var validator: ServiceAreaEntryValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("service area entry $code not found")
        validator.requireValid(item)
        item.moveTo(ServiceAreaEntryStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("service area entry $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServiceAreaEntryStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newServiceAreaEntryService(store: ServiceAreaEntryStore, clock: Clock): ServiceAreaEntryService {
    return ServiceAreaEntryService(
        store = store,
        validator = defaultServiceAreaEntryValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
