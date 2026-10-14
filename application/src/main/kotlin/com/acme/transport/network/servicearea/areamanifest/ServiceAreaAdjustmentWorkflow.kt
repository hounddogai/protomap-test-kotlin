package com.acme.transport.network.servicearea.areamanifest

import com.acme.transport.network.clock.Clock

/** Moves service area adjustments through their life cycle and keeps a journal of what it did. */
data class ServiceAreaAdjustmentDesk(
    var store: ServiceAreaAdjustmentStore,
    var validator: ServiceAreaAdjustmentValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("service area adjustment $code not found")
        validator.requireValid(item)
        item.moveTo(ServiceAreaAdjustmentStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("service area adjustment $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServiceAreaAdjustmentStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newServiceAreaAdjustmentDesk(store: ServiceAreaAdjustmentStore, clock: Clock): ServiceAreaAdjustmentDesk {
    return ServiceAreaAdjustmentDesk(
        store = store,
        validator = defaultServiceAreaAdjustmentValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
