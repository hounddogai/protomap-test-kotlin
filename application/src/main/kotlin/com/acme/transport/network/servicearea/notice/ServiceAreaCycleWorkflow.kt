package com.acme.transport.network.servicearea.notice

import com.acme.transport.network.clock.Clock

/** Moves service area cycles through their life cycle and keeps a journal of what it did. */
data class ServiceAreaCycleWorkflow(
    var store: ServiceAreaCycleStore,
    var validator: ServiceAreaCycleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("service area cycle $code not found")
        validator.requireValid(item)
        item.moveTo(ServiceAreaCycleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("service area cycle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServiceAreaCycleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
    }

    fun scan(cutoff: Int): Int {
        var changed = 0
        for (item in store.list()) {
            if (item.isTerminal()) {
                continue
            }
            if (item.endsAt > cutoff) {
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

fun newServiceAreaCycleWorkflow(store: ServiceAreaCycleStore, clock: Clock): ServiceAreaCycleWorkflow {
    return ServiceAreaCycleWorkflow(
        store = store,
        validator = defaultServiceAreaCycleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
