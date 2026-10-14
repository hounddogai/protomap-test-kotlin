package com.acme.transport.network.servicearea.claim

import com.acme.transport.network.clock.Clock

/** Moves service area claims through their life cycle and keeps a journal of what it did. */
data class ServiceAreaClaimService(
    var store: ServiceAreaClaimStore,
    var validator: ServiceAreaClaimValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("service area claim $code not found")
        validator.requireValid(item)
        item.moveTo(ServiceAreaClaimStatus.VALIDATED)
        store.save(item)
        note("$code is validated")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("service area claim $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServiceAreaClaimStatus.RETIRED)
        store.save(item)
        note("$code is retired: $reason")
    }

    fun scan(cutoff: Int): Int {
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

fun newServiceAreaClaimService(store: ServiceAreaClaimStore, clock: Clock): ServiceAreaClaimService {
    return ServiceAreaClaimService(
        store = store,
        validator = defaultServiceAreaClaimValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
