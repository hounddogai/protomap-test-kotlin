package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.freightpay.clock.Clock

/**
 * Moves primary fuel surcharge bundles through their life cycle and keeps a journal of what it did.
 */
data class PrimaryFuelSurchargeBundleCoordinator(
    var store: PrimaryFuelSurchargeBundleStore,
    var validator: PrimaryFuelSurchargeBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryFuelSurchargeBundleEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun open(code: String) {
        val item = store.find(code) ?: error("primary fuel surcharge bundle $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryFuelSurchargeBundleStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary fuel surcharge bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryFuelSurchargeBundleStatus.STOPPED)
        store.save(item)
        note("$code is stopped: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("primary fuel surcharge bundle $code not found")
        val result = engine.price(item)
        note("$code evaluated at $result")
        return result
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

fun newPrimaryFuelSurchargeBundleCoordinator(store: PrimaryFuelSurchargeBundleStore, clock: Clock): PrimaryFuelSurchargeBundleCoordinator {
    return PrimaryFuelSurchargeBundleCoordinator(
        store = store,
        validator = defaultPrimaryFuelSurchargeBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryFuelSurchargeBundleEngine(),
    )
}
