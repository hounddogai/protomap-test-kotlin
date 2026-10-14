package com.acme.transport.crewing.dutyperiod.bundle

import com.acme.transport.crewing.clock.Clock

/** Moves fallback duty period bundles through their life cycle and keeps a journal of what it did. */
data class FallbackDutyPeriodBundleDesk(
    var store: FallbackDutyPeriodBundleStore,
    var validator: FallbackDutyPeriodBundleValidator,
    var clock: Clock,
    var journal: MutableList<String>,
) {
    private fun note(message: String) {
        journal.add("${clock.now()} $message")
    }

    fun advance(code: String) {
        val item = store.find(code) ?: error("fallback duty period bundle $code not found")
        validator.requireValid(item)
        item.moveTo(FallbackDutyPeriodBundleStatus.SCHEDULED)
        store.save(item)
        note("$code is scheduled")
    }

    fun finish(code: String, reason: String) {
        val item = store.find(code) ?: error("fallback duty period bundle $code not found")
        if (item.isTerminal()) {
            note("$code already closed")
            return
        }
        item.moveTo(FallbackDutyPeriodBundleStatus.CANCELLED)
        store.save(item)
        note("$code is cancelled: $reason")
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

fun newFallbackDutyPeriodBundleDesk(store: FallbackDutyPeriodBundleStore, clock: Clock): FallbackDutyPeriodBundleDesk {
    return FallbackDutyPeriodBundleDesk(
        store = store,
        validator = defaultFallbackDutyPeriodBundleValidator(),
        clock = clock,
        journal = mutableListOf<String>(),
    )
}
