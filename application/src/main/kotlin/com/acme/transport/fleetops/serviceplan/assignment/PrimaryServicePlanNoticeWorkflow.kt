package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.fleetops.clock.Clock

/** Moves primary service plan notices through their life cycle and keeps a journal of what it did. */
data class PrimaryServicePlanNoticeManager(
    var store: PrimaryServicePlanNoticeStore,
    var validator: PrimaryServicePlanNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("primary service plan notice $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryServicePlanNoticeStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("primary service plan notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryServicePlanNoticeStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
    }

    fun sweep(cutoff: Int): Int {
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

fun newPrimaryServicePlanNoticeManager(store: PrimaryServicePlanNoticeStore, clock: Clock): PrimaryServicePlanNoticeManager {
    return PrimaryServicePlanNoticeManager(
        store = store,
        validator = defaultPrimaryServicePlanNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
