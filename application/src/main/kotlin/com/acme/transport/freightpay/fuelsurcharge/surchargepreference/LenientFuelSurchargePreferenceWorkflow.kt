package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves lenient fuel surcharge preferences through their life cycle and keeps a journal of what it did.
 */
data class LenientFuelSurchargePreferenceDesk(
    var store: LenientFuelSurchargePreferenceStore,
    var validator: LenientFuelSurchargePreferenceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("lenient fuel surcharge preference $code not found")
        validator.requireValid(item)
        item.moveTo(LenientFuelSurchargePreferenceStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun retire(code: String, reason: String) {
        val item = store.find(code) ?: error("lenient fuel surcharge preference $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(LenientFuelSurchargePreferenceStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newLenientFuelSurchargePreferenceDesk(store: LenientFuelSurchargePreferenceStore, clock: Clock): LenientFuelSurchargePreferenceDesk {
    return LenientFuelSurchargePreferenceDesk(
        store = store,
        validator = defaultLenientFuelSurchargePreferenceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
