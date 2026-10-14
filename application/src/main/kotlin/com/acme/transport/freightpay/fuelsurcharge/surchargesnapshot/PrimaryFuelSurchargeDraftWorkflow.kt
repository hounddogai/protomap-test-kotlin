package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import com.acme.transport.freightpay.clock.Clock

/** Moves primary fuel surcharge drafts through their life cycle and keeps a journal of what it did. */
data class PrimaryFuelSurchargeDraftManager(
    var store: PrimaryFuelSurchargeDraftStore,
    var validator: PrimaryFuelSurchargeDraftValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: PrimaryFuelSurchargeDraftEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun start(code: String) {
        val item = store.find(code) ?: error("primary fuel surcharge draft $code not found")
        validator.requireValid(item)
        item.moveTo(PrimaryFuelSurchargeDraftStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("primary fuel surcharge draft $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(PrimaryFuelSurchargeDraftStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun estimate(code: String): Double {
        val item = store.find(code) ?: error("primary fuel surcharge draft $code not found")
        val result = engine.evaluate(item)
        note("$code evaluated at $result")
        return result
    }

    fun audit(cutoff: Int): Int {
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

fun newPrimaryFuelSurchargeDraftManager(store: PrimaryFuelSurchargeDraftStore, clock: Clock): PrimaryFuelSurchargeDraftManager {
    return PrimaryFuelSurchargeDraftManager(
        store = store,
        validator = defaultPrimaryFuelSurchargeDraftValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultPrimaryFuelSurchargeDraftEngine(),
    )
}
