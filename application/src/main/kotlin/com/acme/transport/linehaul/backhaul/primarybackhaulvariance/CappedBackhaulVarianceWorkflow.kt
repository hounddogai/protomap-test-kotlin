package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.linehaul.clock.Clock

/** Moves capped backhaul variances through their life cycle and keeps a journal of what it did. */
data class CappedBackhaulVarianceService(
    var store: CappedBackhaulVarianceStore,
    var validator: CappedBackhaulVarianceValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun submit(code: String) {
        val item = store.find(code) ?: error("capped backhaul variance $code not found")
        validator.requireValid(item)
        item.moveTo(CappedBackhaulVarianceStatus.WARMING)
        store.save(item)
        note("$code is warming")
    }

    fun settle(code: String, reason: String) {
        val item = store.find(code) ?: error("capped backhaul variance $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(CappedBackhaulVarianceStatus.STOPPED)
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

fun newCappedBackhaulVarianceService(store: CappedBackhaulVarianceStore, clock: Clock): CappedBackhaulVarianceService {
    return CappedBackhaulVarianceService(
        store = store,
        validator = defaultCappedBackhaulVarianceValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
