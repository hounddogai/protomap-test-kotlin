package com.acme.transport.network.servicearea.assignment

import com.acme.transport.network.clock.Clock

/** Moves service area allocations through their life cycle and keeps a journal of what it did. */
data class ServiceAreaAllocationWorkflow(
    var store: ServiceAreaAllocationStore,
    var validator: ServiceAreaAllocationValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("service area allocation $code not found")
        validator.requireValid(item)
        item.moveTo(ServiceAreaAllocationStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("service area allocation $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServiceAreaAllocationStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newServiceAreaAllocationWorkflow(store: ServiceAreaAllocationStore, clock: Clock): ServiceAreaAllocationWorkflow {
    return ServiceAreaAllocationWorkflow(
        store = store,
        validator = defaultServiceAreaAllocationValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
