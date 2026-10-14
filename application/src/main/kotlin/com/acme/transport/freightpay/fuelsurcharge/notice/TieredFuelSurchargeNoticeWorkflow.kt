package com.acme.transport.freightpay.fuelsurcharge.notice

import com.acme.transport.freightpay.clock.Clock

/** Moves tiered fuel surcharge notices through their life cycle and keeps a journal of what it did. */
data class TieredFuelSurchargeNoticeManager(
    var store: TieredFuelSurchargeNoticeStore,
    var validator: TieredFuelSurchargeNoticeValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("tiered fuel surcharge notice $code not found")
        validator.requireValid(item)
        item.moveTo(TieredFuelSurchargeNoticeStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun close(code: String, reason: String) {
        val item = store.find(code) ?: error("tiered fuel surcharge notice $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(TieredFuelSurchargeNoticeStatus.STOPPED)
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

fun newTieredFuelSurchargeNoticeManager(store: TieredFuelSurchargeNoticeStore, clock: Clock): TieredFuelSurchargeNoticeManager {
    return TieredFuelSurchargeNoticeManager(
        store = store,
        validator = defaultTieredFuelSurchargeNoticeValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
