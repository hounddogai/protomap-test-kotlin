package com.acme.transport.freightpay.fuelsurcharge.strictsurchargesnapshot

import com.acme.transport.freightpay.clock.Clock

/** Moves strict fuel surcharge targets through their life cycle and keeps a journal of what it did. */
data class StrictFuelSurchargeTargetManager(
    var store: StrictFuelSurchargeTargetStore,
    var validator: StrictFuelSurchargeTargetValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("strict fuel surcharge target $code not found")
        validator.requireValid(item)
        item.moveTo(StrictFuelSurchargeTargetStatus.PENDING)
        store.save(item)
        note("$code is pending")
    }

    fun complete(code: String, reason: String) {
        val item = store.find(code) ?: error("strict fuel surcharge target $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(StrictFuelSurchargeTargetStatus.CLOSED)
        store.save(item)
        note("$code is closed: $reason")
    }

    fun reviewStale(cutoff: Int): Int {
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

fun newStrictFuelSurchargeTargetManager(store: StrictFuelSurchargeTargetStore, clock: Clock): StrictFuelSurchargeTargetManager {
    return StrictFuelSurchargeTargetManager(
        store = store,
        validator = defaultStrictFuelSurchargeTargetValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
