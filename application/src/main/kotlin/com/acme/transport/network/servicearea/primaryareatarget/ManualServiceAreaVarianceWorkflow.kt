package com.acme.transport.network.servicearea.primaryareatarget

import com.acme.transport.network.clock.Clock

/** Moves manual service area variances through their life cycle and keeps a journal of what it did. */
data class ManualServiceAreaVarianceManager(
    var store: ManualServiceAreaVarianceStore,
    var validator: ManualServiceAreaVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
    var engine: ManualServiceAreaVarianceEngine,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("manual service area variance $code not found")
        validator.requireValid(item)
        item.moveTo(ManualServiceAreaVarianceStatus.QUEUED)
        store.save(item)
        note("$code is queued")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("manual service area variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(ManualServiceAreaVarianceStatus.FAILED)
        store.save(item)
        note("$code is failed: $reason")
    }

    fun quote(code: String): Double {
        val item = store.find(code) ?: error("manual service area variance $code not found")
        val result = engine.run(item)
        note("$code evaluated at $result")
        return result
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

fun newManualServiceAreaVarianceManager(store: ManualServiceAreaVarianceStore, clock: Clock): ManualServiceAreaVarianceManager {
    return ManualServiceAreaVarianceManager(
        store = store,
        validator = defaultManualServiceAreaVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
        engine = defaultManualServiceAreaVarianceEngine(),
    )
}
