package com.acme.transport.network.servicearea.notice

import com.acme.transport.network.clock.Clock

/** Moves service area notices through their life cycle and keeps a journal of what it did. */
data class ServiceAreaNoticeCoordinator(
    var store: ServiceAreaNoticeStore,
    var validator: ServiceAreaNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("service area notice $code not found")
        validator.requireValid(item)
        item.moveTo(ServiceAreaNoticeStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("service area notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ServiceAreaNoticeStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
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

fun newServiceAreaNoticeCoordinator(store: ServiceAreaNoticeStore, clock: Clock): ServiceAreaNoticeCoordinator {
    return ServiceAreaNoticeCoordinator(
        store = store,
        validator = defaultServiceAreaNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
