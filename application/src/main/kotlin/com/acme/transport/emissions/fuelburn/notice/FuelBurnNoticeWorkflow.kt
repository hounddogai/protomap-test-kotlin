package com.acme.transport.emissions.fuelburn.notice

import com.acme.transport.emissions.clock.Clock

/** Moves fuel burn notices through their life cycle and keeps a journal of what it did. */
data class FuelBurnNoticeWorkflow(
    var store: FuelBurnNoticeStore,
    var validator: FuelBurnNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("fuel burn notice $code not found")
        validator.requireValid(item)
        item.moveTo(FuelBurnNoticeStatus.APPROVED)
        store.save(item)
        note("$code is approved")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel burn notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelBurnNoticeStatus.WITHDRAWN)
        store.save(item)
        note("$code is withdrawn: $reason")
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

fun newFuelBurnNoticeWorkflow(store: FuelBurnNoticeStore, clock: Clock): FuelBurnNoticeWorkflow {
    return FuelBurnNoticeWorkflow(
        store = store,
        validator = defaultFuelBurnNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
