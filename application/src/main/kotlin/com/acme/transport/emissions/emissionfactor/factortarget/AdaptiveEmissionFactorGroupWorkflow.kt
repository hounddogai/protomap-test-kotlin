package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.emissions.clock.Clock

/**
 * Moves adaptive emission factor groups through their life cycle and keeps a journal of what it did.
 */
data class AdaptiveEmissionFactorGroupManager(
    var store: AdaptiveEmissionFactorGroupStore,
    var validator: AdaptiveEmissionFactorGroupValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("adaptive emission factor group $code not found")
        validator.requireValid(item)
        item.moveTo(AdaptiveEmissionFactorGroupStatus.GRANTED)
        store.save(item)
        note("$code is granted")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("adaptive emission factor group $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(AdaptiveEmissionFactorGroupStatus.EXPIRED)
        store.save(item)
        note("$code is expired: $reason")
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

fun newAdaptiveEmissionFactorGroupManager(store: AdaptiveEmissionFactorGroupStore, clock: Clock): AdaptiveEmissionFactorGroupManager {
    return AdaptiveEmissionFactorGroupManager(
        store = store,
        validator = defaultAdaptiveEmissionFactorGroupValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
