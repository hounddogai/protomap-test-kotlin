package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.network.clock.Clock

/** Moves service area agreements through their life cycle and keeps a journal of what it did. */
data class ServiceAreaAgreementManager(
    var store: ServiceAreaAgreementStore,
    var validator: ServiceAreaAgreementValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("service area agreement $code not found")
        validator.requireValid(item)
        item.moveTo(ServiceAreaAgreementStatus.IN_REVIEW)
        store.save(item)
        note("$code is in review")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("service area agreement $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServiceAreaAgreementStatus.ARCHIVED)
        store.save(item)
        note("$code is archived: $reason")
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

fun newServiceAreaAgreementManager(store: ServiceAreaAgreementStore, clock: Clock): ServiceAreaAgreementManager {
    return ServiceAreaAgreementManager(
        store = store,
        validator = defaultServiceAreaAgreementValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
