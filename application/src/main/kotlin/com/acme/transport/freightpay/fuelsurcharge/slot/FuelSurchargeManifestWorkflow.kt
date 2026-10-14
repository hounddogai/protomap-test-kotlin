package com.acme.transport.freightpay.fuelsurcharge.slot

import com.acme.transport.freightpay.clock.Clock

/** Moves fuel surcharge manifests through their life cycle and keeps a journal of what it did. */
data class FuelSurchargeManifestManager(
    var store: FuelSurchargeManifestStore,
    var validator: FuelSurchargeManifestValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: FuelSurchargeManifestEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("fuel surcharge manifest $code not found")
        validator.requireValid(item)
        item.moveTo(FuelSurchargeManifestStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("fuel surcharge manifest $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FuelSurchargeManifestStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("fuel surcharge manifest $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newFuelSurchargeManifestManager(store: FuelSurchargeManifestStore, clock: Clock): FuelSurchargeManifestManager {
    return FuelSurchargeManifestManager(
        store = store,
        validator = defaultFuelSurchargeManifestValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultFuelSurchargeManifestEngine(),
    )
}
