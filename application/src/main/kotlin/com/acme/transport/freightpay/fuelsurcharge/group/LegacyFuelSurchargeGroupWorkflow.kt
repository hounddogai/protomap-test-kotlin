package com.acme.transport.freightpay.fuelsurcharge.group

import com.acme.transport.freightpay.clock.Clock

/** Moves legacy fuel surcharge groups through their life cycle and keeps a journal of what it did. */
data class LegacyFuelSurchargeGroupDesk(
    var store: LegacyFuelSurchargeGroupStore,
    var validator: LegacyFuelSurchargeGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("legacy fuel surcharge group $code not found")
        validator.requireValid(item)
        item.moveTo(LegacyFuelSurchargeGroupStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("legacy fuel surcharge group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LegacyFuelSurchargeGroupStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
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

fun newLegacyFuelSurchargeGroupDesk(store: LegacyFuelSurchargeGroupStore, clock: Clock): LegacyFuelSurchargeGroupDesk {
    return LegacyFuelSurchargeGroupDesk(
        store = store,
        validator = defaultLegacyFuelSurchargeGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
